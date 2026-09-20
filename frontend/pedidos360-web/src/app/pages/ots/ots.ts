import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, Ot, OtInput, OtItem, OtItemInput } from '../../core/api.service';

@Component({
  selector: 'app-ots',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule],
  template: `
    <section class="page-shell page-heading">
      <div>
        <p class="eyebrow">Taller automotriz</p>
        <h1>Ordenes de trabajo</h1>
      </div>
      <div class="counter">{{ ots().length }} registros</div>
    </section>

    <section class="page-shell content-grid">
      <div>
        @if (loading()) { <p class="notice">Consultando Oracle mediante el BFF...</p> }
        @if (error()) { <p class="notice notice-error">{{ error() }}</p> }
        @if (!loading() && !error() && ots().length === 0) {
          <p class="empty-state">No hay ordenes de trabajo registradas.</p>
        }

        <div class="record-list">
          @for (ot of ots(); track ot.id) {
            <article class="record-card">
              <div>
                <span class="record-id">{{ ot.id }}</span>
                <h2>{{ ot.patente }} · {{ ot.clienteId }}</h2>
                <p>{{ ot.descripcion || 'Sin descripcion' }}</p>
                <small>{{ ot.createdAt | date:'dd/MM/yyyy HH:mm' }}</small>
              </div>
              <div class="record-metrics">
                <strong>{{ ot.total | currency:'CLP':'symbol-narrow':'1.0-0' }}</strong>
                <button class="text-button" type="button" (click)="selectOt(ot)">Ver items</button>
              </div>
            </article>
          }
        </div>

        @if (selectedOtId()) {
          <section class="form-card">
            <p class="eyebrow">{{ selectedOtId() }}</p>
            <h2>Items de la OT</h2>
            @if (items().length === 0) { <p class="empty-state">Esta OT no tiene items.</p> }
            <div class="record-list">
              @for (item of items(); track item.id) {
                <article class="record-card">
                  <div>
                    <span class="record-id">Item #{{ item.id }}</span>
                    <h2>{{ item.concepto }}</h2>
                    <p>{{ item.cantidad | number:'1.0-2' }} × {{ item.precioUnit | currency:'CLP':'symbol-narrow':'1.0-0' }}</p>
                  </div>
                  <div class="record-metrics">
                    <strong>{{ item.subtotal | currency:'CLP':'symbol-narrow':'1.0-0' }}</strong>
                  </div>
                </article>
              }
            </div>
            <form (ngSubmit)="createItem()">
              <label>Concepto <input name="concepto" [(ngModel)]="itemForm.concepto" required maxlength="40"></label>
              <div class="form-row">
                <label>Cantidad <input name="cantidad" type="number" [(ngModel)]="itemForm.cantidad" min="0.01" step="0.01" required></label>
                <label>Precio unitario <input name="precioUnit" type="number" [(ngModel)]="itemForm.precioUnit" min="0" step="1" required></label>
              </div>
              <button class="button button-secondary button-block" type="submit" [disabled]="saving()">
                Agregar item
              </button>
            </form>
          </section>
        }
      </div>

      <aside class="form-card">
        <p class="eyebrow">Rol USER o ADMIN</p>
        <h2>Nueva OT</h2>
        <form (ngSubmit)="createOt()">
          <label>Cliente <input name="clienteId" [(ngModel)]="form.clienteId" required maxlength="20"></label>
          <label>Patente <input name="patente" [(ngModel)]="form.patente" required maxlength="10"></label>
          <label>Descripcion <textarea name="descripcion" [(ngModel)]="form.descripcion" maxlength="200"></textarea></label>
          <label>Total <input name="total" type="number" [(ngModel)]="form.total" min="0" step="1" required></label>
          <button class="button button-primary button-block" type="submit" [disabled]="saving()">
            {{ saving() ? 'Guardando...' : 'Crear OT' }}
          </button>
        </form>
      </aside>
    </section>
  `,
})
export class Ots implements OnInit {
  private readonly api = inject(ApiService);
  readonly ots = signal<Ot[]>([]);
  readonly items = signal<OtItem[]>([]);
  readonly selectedOtId = signal<string | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  form: OtInput = { clienteId: '', patente: '', descripcion: '', total: 0 };
  itemForm: OtItemInput = { concepto: '', cantidad: 1, precioUnit: 0 };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.getOts().subscribe({
      next: (ots) => {
        this.ots.set(ots);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.messageFor(error));
        this.loading.set(false);
      },
    });
  }

  createOt(): void {
    this.saving.set(true);
    this.error.set('');
    this.api.createOt(this.form).subscribe({
      next: () => {
        this.form = { clienteId: '', patente: '', descripcion: '', total: 0 };
        this.saving.set(false);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.handleSaveError(error),
    });
  }

  selectOt(ot: Ot): void {
    this.selectedOtId.set(ot.id);
    this.loadItems(ot.id);
  }

  createItem(): void {
    const otId = this.selectedOtId();
    if (!otId) return;
    this.saving.set(true);
    this.error.set('');
    this.api.createOtItem(otId, this.itemForm).subscribe({
      next: () => {
        this.itemForm = { concepto: '', cantidad: 1, precioUnit: 0 };
        this.saving.set(false);
        this.loadItems(otId);
      },
      error: (error: HttpErrorResponse) => this.handleSaveError(error),
    });
  }

  private loadItems(otId: string): void {
    this.api.getOtItems(otId).subscribe({
      next: (items) => this.items.set(items),
      error: (error: HttpErrorResponse) => this.error.set(this.messageFor(error)),
    });
  }

  private handleSaveError(error: HttpErrorResponse): void {
    this.saving.set(false);
    this.error.set(this.messageFor(error));
  }

  private messageFor(error: HttpErrorResponse): string {
    if (error.status === 401) return 'La sesion no es valida. Ingresa nuevamente.';
    if (error.status === 403) return 'No tienes permisos para esta operacion.';
    if (error.status === 0) return 'No fue posible conectar con el BFF en este momento.';
    return error.error?.message ?? 'No fue posible completar la operacion.';
  }
}
