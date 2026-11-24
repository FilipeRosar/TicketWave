import { Routes } from '@angular/router';
import { AuthPage } from './auth/auth-page';
import { Home } from './home/home';

export const routes: Routes = [
  { path: '', component: Home, title: 'TicketWave | Descubra eventos' },
  { path: 'entrar', component: AuthPage, data: { mode: 'login' }, title: 'Entrar | TicketWave' },
  { path: 'cadastro', component: AuthPage, data: { mode: 'register' }, title: 'Criar conta | TicketWave' },
  { path: 'conta', component: AuthPage, data: { mode: 'account' }, title: 'Minha conta | TicketWave' },
  { path: '**', redirectTo: '' },
];
