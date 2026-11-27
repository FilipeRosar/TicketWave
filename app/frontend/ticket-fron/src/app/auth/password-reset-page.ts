import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApi } from '../api/auth-api';

@Component({
  selector: 'app-password-reset-page',
  imports: [RouterLink],
  templateUrl: './password-reset-page.html',
  styleUrl: './auth-page.css',
})
export class PasswordResetPage {
  private readonly api = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute).snapshot;
  private readonly token = this.route.fragment ?? '';
  protected readonly resetting = this.route.data['mode'] === 'reset';
  protected readonly validLink = /^[A-Za-z0-9_-]{43}$/.test(this.token);
  protected readonly busy = signal(false);
  protected readonly requested = signal(false);
  protected readonly error = signal('');

  protected async submit(event: SubmitEvent, form: HTMLFormElement): Promise<void> {
    event.preventDefault();
    if (this.busy() || (this.resetting && !this.validLink)) return;
    const data = new FormData(form);
    const password = String(data.get('password') ?? '');
    const passwordConfirmation = String(data.get('passwordConfirmation') ?? '');
    if (this.resetting && password !== passwordConfirmation) {
      this.error.set('As senhas devem ser iguais.');
      return;
    }

    this.error.set('');
    this.busy.set(true);
    try {
      if (this.resetting) {
        await this.api.resetPassword({ token: this.token, password, passwordConfirmation });
        await this.router.navigate(['/entrar'], { replaceUrl: true, state: { passwordReset: true } });
      } else {
        await this.api.forgotPassword(String(data.get('email')).trim());
        this.requested.set(true);
      }
    } catch (error) {
      const response = error instanceof HttpErrorResponse ? error.error : undefined;
      this.error.set(response?.violations?.[0]?.message ?? response?.message ?? 'Não foi possível concluir. Tente novamente.');
    } finally {
      this.busy.set(false);
    }
  }
}
