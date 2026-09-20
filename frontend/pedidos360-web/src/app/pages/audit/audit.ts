import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ApiService, NotifyLog, OtEvent } from '../../core/api.service';

@Component({
  selector: 'app-audit',
  imports: [DatePipe],
  template: `
    <section class="page-shell page-heading">
      <div>
        <p class="eyebrow">Rol ADMIN</p>
        <h1>Auditoria y notificaciones</h1>
      </div>
      <div class="counter">{{ events().length + notifications().length }} registros</div>
    </section>

    <section class="page-shell content-grid">
      <div>
        <h2>Eventos de OT</h2>
        @if (loading()) { <p class="notice">Consultando el BFF...</p> }
        @if (error()) { <p class="notice notice-error">{{ error() }}</p> }
        <div class="record-list">
          @for (event of events(); track event.id) {
            <article class="record-card">
              <div>
                <span class="record-id">Evento #{{ event.id }}</span>
                <h2>{{ event.eventType }}</h2>
                <p>{{ event.otId || 'Sin OT asociada' }} · {{ event.createdAt | date:'dd/MM/yyyy HH:mm' }}</p>
                <code>{{ event.payloadJson }}</code>
              </div>
            </article>
          }
        </div>
      </div>

      <div>
        <h2>Notificaciones</h2>
        <div class="record-list">
          @for (notification of notifications(); track notification.id) {
            <article class="record-card">
              <div>
                <span class="record-id">Notificacion #{{ notification.id }}</span>
                <h2>{{ notification.canal || 'Sin canal' }}</h2>
                <p>{{ notification.otId || 'Sin OT' }} · {{ notification.clienteId || 'Sin cliente' }}</p>
                <p>{{ notification.createdAt | date:'dd/MM/yyyy HH:mm' }}</p>
                <code>{{ notification.payloadJson }}</code>
              </div>
            </article>
          }
        </div>
      </div>
    </section>
  `,
})
export class Audit implements OnInit {
  private readonly api = inject(ApiService);
  readonly events = signal<OtEvent[]>([]);
  readonly notifications = signal<NotifyLog[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    forkJoin({ events: this.api.getEvents(), notifications: this.api.getNotifications() }).subscribe({
      next: ({ events, notifications }) => {
        this.events.set(events);
        this.notifications.set(notifications);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.messageFor(error));
        this.loading.set(false);
      },
    });
  }

  private messageFor(error: HttpErrorResponse): string {
    if (error.status === 401) return 'La sesion no es valida. Ingresa nuevamente.';
    if (error.status === 403) return 'La auditoria requiere rol ADMIN.';
    if (error.status === 0) return 'No fue posible conectar con el BFF en este momento.';
    return error.error?.message ?? 'No fue posible consultar la auditoria.';
  }
}
