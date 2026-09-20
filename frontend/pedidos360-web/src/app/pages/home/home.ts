import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  template: `
    <section class="hero page-shell">
      <div class="eyebrow">Arquitectura Cloud Native</div>
      <h1>Pedidos simples.<br><em>Seguridad real.</em></h1>
      <p class="hero-copy">
        Una base demostrable para administrar ordenes de trabajo con identidad Microsoft,
        un BFF seguro y microservicios Spring Boot.
      </p>
      <div class="hero-actions">
        @if (auth.isAuthenticated()) {
          <a class="button button-primary" routerLink="/ots">Gestionar ordenes</a>
          @if (auth.hasRole('ADMIN')) {
            <a class="button button-secondary" routerLink="/audit">Ver auditoria</a>
          }
        } @else {
          <a class="button button-primary" routerLink="/login">Iniciar sesion segura</a>
        }
      </div>
    </section>

    <section class="page-shell architecture-strip" aria-label="Flujo de arquitectura">
      <span>Angular</span><b>01</b>
      <span>Entra ID</span><b>02</b>
      <span>API Gateway</span><b>03</b>
      <span>BFF</span><b>04</b>
      <span>Microservicios</span>
    </section>

    @if (auth.isAuthenticated()) {
      <section class="page-shell identity-panel">
        <div>
          <p class="eyebrow">Sesion autenticada</p>
          <h2>{{ auth.displayName() }}</h2>
          <p>{{ auth.email() }}</p>
        </div>
        <div>
          <span class="label">Roles JWT</span>
          <div class="tags">
            @for (role of auth.roles(); track role) { <span class="tag">{{ role }}</span> }
            @if (auth.roles().length === 0) { <span class="tag tag-muted">Sin roles asignados</span> }
          </div>
        </div>
        <div>
          <span class="label">Scopes JWT</span>
          <div class="tags">
            @for (scope of auth.scopes(); track scope) { <span class="tag">{{ scope }}</span> }
            @if (auth.scopes().length === 0) { <span class="tag tag-muted">Sin scopes disponibles</span> }
          </div>
        </div>
      </section>
    }
  `,
})
export class Home {
  readonly auth = inject(AuthService);
}
