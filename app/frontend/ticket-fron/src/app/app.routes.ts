import { Routes } from '@angular/router';
import { AccountPage } from './account/account-page';
import { AuthPage } from './auth/auth-page';
import { PasswordResetPage } from './auth/password-reset-page';
import { Home } from './home/home';

export const routes: Routes = [
  { path: '', component: Home, title: 'TicketWave | Descubra eventos' },
  { path: 'entrar', component: AuthPage, data: { mode: 'login' }, title: 'Entrar | TicketWave' },
  { path: 'cadastro', component: AuthPage, data: { mode: 'register' }, title: 'Criar conta | TicketWave' },
  { path: 'esqueci-minha-senha', component: PasswordResetPage, title: 'Recuperar senha | TicketWave' },
  { path: 'redefinir-senha', component: PasswordResetPage, data: { mode: 'reset' }, title: 'Redefinir senha | TicketWave' },
  { path: 'conta', component: AccountPage, title: 'Minha conta | TicketWave' },
  { path: '**', redirectTo: '' },
];
