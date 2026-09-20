import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Audit } from './pages/audit/audit';
import { Ots } from './pages/ots/ots';

export const routes: Routes = [
  { path: '', component: Home, title: 'Inicio | Pedidos360' },
  { path: 'login', component: Login, title: 'Ingresar | Pedidos360' },
  { path: 'ots', component: Ots, canActivate: [MsalGuard], title: 'Ordenes de trabajo | Pedidos360' },
  { path: 'audit', component: Audit, canActivate: [MsalGuard], title: 'Auditoria | Pedidos360' },
  { path: '**', redirectTo: '' },
];
