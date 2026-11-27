import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ThemeToggle } from '../theme/theme-toggle';
import { AuthSession } from '../auth/auth-session';
import { ParticipantProfilePage } from '../profile/participant-profile-page';

@Component({
  selector: 'app-account-page',
  imports: [NgTemplateOutlet, RouterLink, ParticipantProfilePage, ThemeToggle],
  templateUrl: './account-page.html',
  styleUrl: './account-page.css',
})
export class AccountPage {
  private readonly router = inject(Router);
  protected readonly session = inject(AuthSession);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  protected async logout(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      await this.session.logout();
      await this.router.navigateByUrl('/');
    } catch (error) {
      const message = error instanceof HttpErrorResponse ? error.error?.message : undefined;
      this.error.set(typeof message === 'string' ? message : 'Não foi possível sair da conta. Tente novamente.');
    } finally {
      this.busy.set(false);
    }
  }
}
