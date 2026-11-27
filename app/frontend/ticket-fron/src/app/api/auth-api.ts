import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { Profile, Registration, Tokens } from '../interfaces/auth';

const authUrl = '/v1/api/auth';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  register(details: Registration): Promise<void> {
    return firstValueFrom(this.http.post<void>(authUrl + '/register', details));
  }

  login(email: string, password: string): Promise<Tokens> {
    return firstValueFrom(this.http.post<Tokens>(authUrl + '/login', { email, password }));
  }

  getProfile(accessToken: string): Promise<Profile> {
    return firstValueFrom(this.http.get<Profile>(authUrl + '/me', {
      headers: { Authorization: 'Bearer ' + accessToken },
    }));
  }

  refresh(refreshToken: string): Promise<Tokens> {
    return firstValueFrom(this.http.post<Tokens>(authUrl + '/refresh', { refreshToken }));
  }

  logout(refreshToken: string): Promise<void> {
    return firstValueFrom(this.http.post<void>(authUrl + '/logout', { refreshToken }));
  }
}
