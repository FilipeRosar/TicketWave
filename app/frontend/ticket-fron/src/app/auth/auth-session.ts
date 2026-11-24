import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

interface Tokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface Profile {
  name: string;
  email: string;
}

export interface Registration {
  name: string;
  email: string;
  phone: string;
  password: string;
}

const api = '/v1/api/auth';

@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly http = inject(HttpClient);
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

  async register(details: Registration): Promise<void> {
    await firstValueFrom(this.http.post<void>(api + '/register', details));
  }

  async login(email: string, password: string): Promise<void> {
    const tokens = await firstValueFrom(this.http.post<Tokens>(api + '/login', { email, password }));
    const profile = await firstValueFrom(this.http.get<Profile>(api + '/me', {
      headers: { Authorization: 'Bearer ' + tokens.accessToken },
    }));
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
      await firstValueFrom(this.http.post<void>(api + '/logout', { refreshToken: this.tokens.refreshToken }));
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
      const refreshed = await firstValueFrom(this.http.post<Tokens>(api + '/refresh', {
        refreshToken: tokens.refreshToken,
      }));
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
