import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  template: `
    <section class="login-layout page-shell">
      <div>
        <p class="eyebrow">Microsoft Entra ID</p>
        <h1>Acceso protegido<br>sin contrasenas locales.</h1>
        <p class="hero-copy">
          MSAL utiliza OpenID Connect y Authorization Code con PKCE. Pedidos360 nunca recibe
          ni almacena tu contrasena de Microsoft.
        </p>
      </div>
      <div class="login-card">
        @if (auth.isAuthenticated()) {
          <span class="status-dot">Sesion activa</span>
          <h2>{{ auth.displayName() }}</h2>
          <p>{{ auth.email() }}</p>
          <a class="button button-primary button-block" routerLink="/ots">Continuar</a>
        } @else {
          <span class="status-dot">Conexion segura</span>
          <h2>Ingresar a Pedidos360</h2>
          <p>Seras redirigido al portal oficial de Microsoft.</p>
          <button class="button button-primary button-block" type="button" (click)="auth.login()">
            Continuar con Microsoft
          </button>
          <small>Requiere configurar los IDs indicados en <code>environment.ts</code>.</small>
        }
      </div>
    </section>
  `,
})
export class Login {
  readonly auth = inject(AuthService);
}
