import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from './app.routes';
import { AuthSession } from './auth/auth-session';

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
    const fields = {
      name: 'Pessoa Teste',
      phone: '11912345678',
      email: 'teste@example.test',
      birthDate: '1995-04-20',
      cpf: '529.982.247-25',
      password: 'senha-teste',
      passwordConfirmation: 'senha-teste',
    };
    for (const [name, value] of Object.entries(fields)) {
      (form.elements.namedItem(name) as HTMLInputElement).value = value;
    }
    const http = TestBed.inject(HttpTestingController);
    const input = (name: string) => form.elements.namedItem(name) as HTMLInputElement;
    const lookup = harness.routeNativeElement!.querySelector<HTMLButtonElement>('.zip-code button')!;
    const address = {
      zipCode: '01001-000',
      street: 'Praça da Sé',
      number: '12',
      complement: 'Sala 2',
      district: 'Sé',
      city: 'São Paulo',
      state: 'SP',
    };
    input('zipCode').value = '99999-999';
    lookup.click();
    http.expectOne('https://viacep.com.br/ws/99999999/json/').flush({ erro: true });
    await Promise.resolve();
    harness.detectChanges();
    expect(harness.routeNativeElement?.querySelector('#zip-code-status')?.textContent).toContain('CEP não encontrado');

    input('zipCode').value = address.zipCode;
    input('number').value = address.number;
    input('complement').value = address.complement;
    lookup.click();
    http.expectOne('https://viacep.com.br/ws/01001000/json/').flush({
      logradouro: address.street, bairro: address.district, localidade: address.city, uf: address.state,
      complemento: 'lado ímpar',
    });
    await Promise.resolve();
    for (const [name, value] of Object.entries(address)) {
      expect(input(name).value).toBe(value);
    }

    const confirmation = form.elements.namedItem('passwordConfirmation') as HTMLInputElement;
    confirmation.value = 'outra-senha';
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    http.expectNone('/v1/api/auth/register');
    harness.detectChanges();
    expect(harness.routeNativeElement?.querySelector('[role="alert"]')?.textContent).toContain('As senhas devem ser iguais.');

    confirmation.value = fields.passwordConfirmation;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    const request = http.expectOne('/v1/api/auth/register');
    expect(request.request.body).toEqual({ ...fields, address });
    request.flush(null);

    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/entrar');
    expect(harness.routeNativeElement?.querySelector('[role="status"]')?.textContent).toContain('Conta criada');
    expect(harness.routeNativeElement?.querySelector<HTMLInputElement>('#email')?.value).toBe(fields.email);
    http.verify();
  });

  it('cria, edita e exclui o perfil em Minha conta', async () => {
    const harness = await RouterTestingHarness.create('/entrar');
    const http = TestBed.inject(HttpTestingController);
    const login = TestBed.inject(AuthSession).login('teste@example.test', 'senha-teste');
    http.expectOne('/v1/api/auth/login').flush({ accessToken: 'access', refreshToken: 'refresh', expiresIn: 3600000 });
    await Promise.resolve();
    http.expectOne('/v1/api/auth/me').flush({ name: 'Pessoa Teste', email: 'teste@example.test', role: 'ROLE_CUSTOMER' });
    await login;

    const path = '/v1/api/profile/me';
    await harness.navigateByUrl('/conta');
    const view = harness.routeNativeElement!;
    http.expectOne({ url: path, method: 'GET' }).flush({}, { status: 404, statusText: 'Not Found' });
    await Promise.resolve();
    harness.detectChanges();

    const click = (label: string) => {
      Array.from(view.querySelectorAll('button')).find(button => button.textContent?.trim() === label)!.click();
      harness.detectChanges();
    };
    const submit = (name: string) => {
      const form = view.querySelector('form')!;
      (form.elements.namedItem('name') as HTMLInputElement).value = name;
      (form.elements.namedItem('interestList') as HTMLInputElement).value = 'Design, design, Música';
      form.querySelector<HTMLInputElement>('input[value="HIRE"]')!.checked = true;
      form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
      const request = http.expectOne({ url: path, method: 'PUT' });
      expect(request.request.headers.get('Authorization')).toBe('Bearer access');
      expect(request.request.body.get('name')).toBe(name);
      expect(request.request.body.getAll('interests')).toEqual(['design', 'música']);
      expect(request.request.body.getAll('goals')).toEqual(['HIRE']);
      request.flush({
        name, occupation: 'Designer', city: 'São Paulo', bio: 'Gosto de shows.', photoType: null, photo: null,
        interests: ['design', 'música'], goals: ['HIRE'],
      });
    };

    click('Criar perfil');
    submit('Perfil da pessoa');
    await Promise.resolve();
    harness.detectChanges();
    http.expectOne(path + '/suggestions').flush([{
      userId: 'another-participant', name: 'Ana', occupation: null, city: null,
      sharedInterests: ['design'], matchedGoals: ['GET_HIRED'],
    }]);
    await Promise.resolve();
    harness.detectChanges();
    expect(view.textContent).toContain('Gosto de shows.');
    expect(view.querySelector('.suggestion-list')?.textContent).toContain('Ser contratado');
    click('Editar perfil');
    submit('Nome atualizado');
    await Promise.resolve();
    harness.detectChanges();
    http.expectOne(path + '/suggestions').flush([]);
    expect(view.textContent).toContain('Nome atualizado');

    click('Excluir perfil');
    http.expectNone({ url: path, method: 'DELETE' });
    click('Confirmar exclusão');
    http.expectOne({ url: path, method: 'DELETE' }).flush(null, { status: 204, statusText: 'No Content' });
    await Promise.resolve();
    harness.detectChanges();
    expect(view.textContent).toContain('Perfil excluído.');
    expect(view.textContent).toContain('Sair da conta');
    http.verify();
  });
});
