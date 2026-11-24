import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from './app.routes';

describe('Navegação do TicketWave', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('abre o login em uma rota própria a partir da home', async () => {
    const harness = await RouterTestingHarness.create('/');
    const home = harness.routeNativeElement!;
    expect(home.querySelector('h1')?.textContent).toContain('O melhor acontece');

    home.querySelector<HTMLAnchorElement>('a[href="/entrar"]')!.click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/entrar');
    expect(harness.routeNativeElement?.querySelector('h1')?.textContent).toContain('Entre na sua conta');
    expect(harness.routeNativeElement?.querySelector('dialog')).toBeNull();
  });

  it('após cadastro volta ao login com o e-mail preenchido', async () => {
    const harness = await RouterTestingHarness.create('/cadastro');
    const form = harness.routeNativeElement!.querySelector('form')!;
    const fields = { name: 'Pessoa Teste', phone: '11912345678', email: 'teste@example.test', password: 'senha-teste' };
    for (const [name, value] of Object.entries(fields)) {
      (form.elements.namedItem(name) as HTMLInputElement).value = value;
    }
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    const http = TestBed.inject(HttpTestingController);
    const request = http.expectOne('/v1/api/auth/register');
    expect(request.request.body).toEqual(fields);
    request.flush(null);

    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/entrar');
    expect(harness.routeNativeElement?.querySelector('[role="status"]')?.textContent).toContain('Conta criada');
    expect(harness.routeNativeElement?.querySelector<HTMLInputElement>('#email')?.value).toBe(fields.email);
    http.verify();
  });
});
