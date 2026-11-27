import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { AuthApi } from '../api/auth-api';
import type { Profile, Registration, Tokens } from '../interfaces/auth';

@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly api = inject(AuthApi);
  private readonly profile = signal<Profile | null>(null);
  private readonly notice = signal('');
  private tokens?: Tokens;
  private timer?: ReturnType<typeof setTimeout>;
  private renewal?: Promise<void>;

  readonly user = this.profile.asReadonly();
  readonly sessionNotice = this.notice.asReadonly();

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timer));
  }

  register(details: Registration): Promise<void> {
    return this.api.register(details);
  }

  async login(email: string, password: string): Promise<void> {
    const tokens = await this.api.login(email, password);
    const profile = await this.api.getProfile(tokens.accessToken);
    this.profile.set(profile);
    this.notice.set('');
    this.setTokens(tokens);
  }

  async logout(): Promise<void> {
    clearTimeout(this.timer);
    await this.renewal;
    clearTimeout(this.timer);
    if (!this.tokens) return;

    try {
      await this.api.logout(this.tokens.refreshToken);
    } catch (error) {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        this.setTokens(this.tokens);
        throw error;
      }
    }
    this.clear();
  }

  private setTokens(tokens: Tokens): void {
    this.tokens = tokens;
    clearTimeout(this.timer);
    const delay = Math.max(1_000, tokens.expiresIn - Math.min(30_000, tokens.expiresIn / 10));
    this.timer = setTimeout(() => {
      this.renewal = this.renew(tokens).finally(() => { this.renewal = undefined; });
    }, delay);
  }

  private async renew(tokens: Tokens): Promise<void> {
    try {
      const refreshed = await this.api.refresh(tokens.refreshToken);
      this.setTokens(refreshed);
    } catch {
      this.clear();
      this.notice.set('Não foi possível renovar a sessão. Entre novamente.');
    }
  }

  private clear(): void {
    clearTimeout(this.timer);
    this.tokens = undefined;
    this.profile.set(null);
  }
}
