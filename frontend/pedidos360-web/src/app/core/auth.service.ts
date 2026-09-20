import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MsalBroadcastService, MsalService } from '@azure/msal-angular';
import { AccountInfo, AuthenticationResult, InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs';
import { environment } from '../../environments/environment';

type JwtClaims = Record<string, unknown>;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly msal = inject(MsalService);
  private readonly broadcast = inject(MsalBroadcastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly account = signal<AccountInfo | null>(null);
  readonly accessTokenClaims = signal<JwtClaims>({});
  readonly isAuthenticated = computed(() => this.account() !== null);
  readonly displayName = computed(() => this.account()?.name ?? this.account()?.username ?? 'Usuario');
  readonly email = computed(() => this.account()?.username ?? '');
  readonly roles = computed(() => this.readStringArrayClaim('roles'));
  readonly scopes = computed(() => {
    const scopeClaim = this.accessTokenClaims()['scp'];
    return typeof scopeClaim === 'string' ? scopeClaim.split(' ').filter(Boolean) : [];
  });

  constructor() {
    this.msal.handleRedirectObservable()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          if (result?.account) {
            this.msal.instance.setActiveAccount(result.account);
          }
          this.refreshAccount();
        },
        error: () => this.refreshAccount(),
      });

    this.broadcast.inProgress$
      .pipe(
        filter((status) => status === InteractionStatus.None),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.syncAccount());
  }

  login(): void {
    this.msal.loginRedirect({ scopes: [environment.msal.apiScope] }).subscribe();
  }

  logout(): void {
    this.msal.logoutRedirect({ postLogoutRedirectUri: environment.msal.redirectUri }).subscribe();
  }

  hasRole(role: 'USER' | 'ADMIN'): boolean {
    return this.roles().includes(role);
  }

  private refreshAccount(): void {
    const current = this.syncAccount();
    if (!current) {
      this.accessTokenClaims.set({});
      return;
    }

    this.msal.instance.setActiveAccount(current);
    this.msal.acquireTokenSilent({ account: current, scopes: [environment.msal.apiScope] })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result: AuthenticationResult) => this.accessTokenClaims.set(this.decodeJwt(result.accessToken)),
        error: () => this.accessTokenClaims.set({}),
      });
  }

  private syncAccount(): AccountInfo | null {
    const current = this.msal.instance.getActiveAccount() ?? this.msal.instance.getAllAccounts()[0] ?? null;
    this.account.set(current);
    if (current && !this.msal.instance.getActiveAccount()) {
      this.msal.instance.setActiveAccount(current);
    }
    return current;
  }

  private readStringArrayClaim(name: string): string[] {
    const accessClaim = this.accessTokenClaims()[name];
    if (Array.isArray(accessClaim)) {
      return accessClaim.filter((value): value is string => typeof value === 'string');
    }
    const idClaim = this.account()?.idTokenClaims?.[name];
    return Array.isArray(idClaim)
      ? idClaim.filter((value): value is string => typeof value === 'string')
      : [];
  }

  private decodeJwt(token: string): JwtClaims {
    try {
      const encodedPayload = token.split('.')[1];
      const base64 = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64)
          .split('')
          .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`)
          .join(''),
      );
      return JSON.parse(json) as JwtClaims;
    } catch {
      return {};
    }
  }
}
