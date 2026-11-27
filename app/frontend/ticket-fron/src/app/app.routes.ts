import { Routes } from '@angular/router';
import { AccountPage } from './account/account-page';
import { AuthPage } from './auth/auth-page';
import { Home } from './home/home';

export const routes: Routes = [
  { path: '', component: Home, title: 'TicketWave | Descubra eventos' },
  { path: 'entrar', component: AuthPage, data: { mode: 'login' }, title: 'Entrar | TicketWave' },
  { path: 'cadastro', component: AuthPage, data: { mode: 'register' }, title: 'Criar conta | TicketWave' },
  { path: 'conta', component: AccountPage, title: 'Minha conta | TicketWave' },
  { path: '**', redirectTo: '' },
];
