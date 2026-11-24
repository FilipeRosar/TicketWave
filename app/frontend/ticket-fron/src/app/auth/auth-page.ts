import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthSession } from './auth-session';

@Component({
  selector: 'app-auth-page',
  imports: [RouterLink],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.css',
})
export class AuthPage {
  private readonly router = inject(Router);
  private readonly mode = inject(ActivatedRoute).snapshot.data['mode'];
  private readonly registration = this.router.currentNavigation()?.extras.state;
  protected readonly session = inject(AuthSession);
  protected readonly registering = this.mode === 'register';
  protected readonly showingAccount = this.mode === 'account';
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly passwordVisible = signal(false);
  protected readonly email = typeof this.registration?.['email'] === 'string' ? this.registration['email'] : '';

  protected async submit(event: SubmitEvent, form: HTMLFormElement): Promise<void> {
    event.preventDefault();
    if (this.busy()) return;

    const data = new FormData(form);
    const email = String(data.get('email')).trim();
    const password = String(data.get('password'));
    this.busy.set(true);
    this.error.set('');

    try {
      if (this.registering) {
        await this.session.register({
          name: String(data.get('name')).trim(),
          email,
          phone: String(data.get('phone')).trim(),
          password,
        });
        await this.router.navigate(['/entrar'], { state: { email, registered: true } });
      } else {
        await this.session.login(email, password);
        await this.router.navigateByUrl('/');
      }
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  protected async logout(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      await this.session.logout();
      await this.router.navigateByUrl('/');
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  protected get registered(): boolean {
    return this.registration?.['registered'] === true;
  }

  private showError(error: unknown): void {
    const message = error instanceof HttpErrorResponse ? error.error?.message : undefined;
    this.error.set(typeof message === 'string' ? message : 'Não foi possível concluir. Tente novamente.');
  }
}
