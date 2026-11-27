import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, ElementRef, inject, model, OnInit, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthApi } from '../api/auth-api';
import { GoogleSignInSdkLoader } from '../api/google-sign-in-sdk';
import { AuthSession } from './auth-session';

@Component({
  selector: 'app-google-sign-in',
  templateUrl: './google-sign-in.html',
  styleUrl: './google-sign-in.css',
})
export class GoogleSignIn implements OnInit {
  private readonly api = inject(AuthApi);
  private readonly sdk = inject(GoogleSignInSdkLoader);
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);
  private readonly destroy = inject(DestroyRef);
  private readonly button = viewChild.required<ElementRef<HTMLDivElement>>('googleButton');
  private credential?: string;

  readonly working = model(false);
  protected readonly available = signal(false);
  protected readonly linking = signal(false);
  protected readonly error = signal('');

  async ngOnInit(): Promise<void> {
    await this.prepare();
  }

  protected async confirm(event: SubmitEvent, password: string): Promise<void> {
    event.preventDefault();
    if (this.credential) await this.signIn(this.credential, password);
  }

  protected async cancelLink(): Promise<void> {
    this.credential = undefined;
    this.linking.set(false);
    this.error.set('');
    await this.prepare();
  }

  private async prepare(): Promise<void> {
    try {
      const challenge = await this.api.googleChallenge();
      if (!challenge || this.destroy.destroyed) return;
      this.available.set(true);

      const sdk = await this.sdk.load();
      if (this.destroy.destroyed) return;
      const container = this.button().nativeElement;
      sdk.initialize({
        client_id: challenge.clientId,
        nonce: challenge.nonce,
        callback: ({ credential }) => {
          if (!this.destroy.destroyed) void this.signIn(credential);
        },
      });
      container.replaceChildren();
      sdk.renderButton(container, { theme: 'outline', size: 'large', width: '240', locale: 'pt-BR' });
    } catch {
      if (this.available()) this.error.set('Não foi possível carregar o login do Google.');
    }
  }

  private async signIn(credential: string, password?: string): Promise<void> {
    if (this.working()) return;
    this.working.set(true);
    this.error.set('');
    try {
      await this.session.loginWithGoogle(credential, password);
      if (!this.destroy.destroyed) await this.router.navigateByUrl('/');
    } catch (error) {
      if (this.destroy.destroyed) return;
      const response = error instanceof HttpErrorResponse ? error : undefined;
      if (response?.status === 428) {
        this.credential = credential;
        this.linking.set(true);
        this.error.set(password ? 'Senha incorreta. Tente novamente.' : '');
      } else {
        this.credential = undefined;
        this.linking.set(false);
        this.error.set(response?.error?.message ?? 'Não foi possível entrar com o Google. Tente novamente.');
        await this.prepare();
      }
    } finally {
      if (!this.destroy.destroyed) this.working.set(false);
    }
  }
}
