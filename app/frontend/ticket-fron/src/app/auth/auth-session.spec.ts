import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthSession } from './auth-session';

describe('AuthSession', () => {
  afterEach(() => vi.useRealTimers());

  it('carrega o perfil com o JWT, renova a sessão e revoga o refresh atual ao sair', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const session = TestBed.inject(AuthSession);
    const http = TestBed.inject(HttpTestingController);
    vi.useFakeTimers();

    const login = session.login('teste@example.test', 'senha-de-teste');
    const loginRequest = http.expectOne('/v1/api/auth/login');
    expect(loginRequest.request.body.email).toBe('teste@example.test');
    loginRequest.flush({ accessToken: 'access-1', refreshToken: 'refresh-1', expiresIn: 10_000 });
    await Promise.resolve();

    const profileRequest = http.expectOne('/v1/api/auth/me');
    expect(profileRequest.request.headers.get('Authorization')).toBe('Bearer access-1');
    profileRequest.flush({ name: 'Pessoa Teste', email: 'teste@example.test' });
    await login;
    expect(session.user()?.name).toBe('Pessoa Teste');

    await vi.advanceTimersByTimeAsync(9_000);
    const refresh = http.expectOne('/v1/api/auth/refresh');
    expect(refresh.request.body).toEqual({ refreshToken: 'refresh-1' });
    refresh.flush({ accessToken: 'access-2', refreshToken: 'refresh-2', expiresIn: 10_000 });

    const logout = session.logout();
    await vi.advanceTimersByTimeAsync(0);
    const revoke = http.expectOne('/v1/api/auth/logout');
    expect(revoke.request.body).toEqual({ refreshToken: 'refresh-2' });
    revoke.flush(null);
    await logout;
    expect(session.user()).toBeNull();
    http.verify();
  });
});
