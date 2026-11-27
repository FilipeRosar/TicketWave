import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ViaCepApi } from '../api/via-cep-api';
import { AuthSession } from './auth-session';

@Component({
  selector: 'app-auth-page',
  imports: [RouterLink],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.css',
})
export class AuthPage {
  private readonly router = inject(Router);
  private readonly viaCep = inject(ViaCepApi);
  private readonly mode = inject(ActivatedRoute).snapshot.data['mode'];
  private readonly registration = this.router.currentNavigation()?.extras.state;
  protected readonly session = inject(AuthSession);
  protected readonly registering = this.mode === 'register';
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly lookingUpZipCode = signal(false);
  protected readonly zipCodeStatus = signal('');
  protected readonly passwordVisible = signal(false);
  protected readonly email = typeof this.registration?.['email'] === 'string' ? this.registration['email'] : '';

  protected async submit(event: SubmitEvent, form: HTMLFormElement): Promise<void> {
    event.preventDefault();
    if (this.busy() || this.lookingUpZipCode()) return;

    const data = new FormData(form);
    const email = String(data.get('email')).trim();
    const password = String(data.get('password'));
    const passwordConfirmation = String(data.get('passwordConfirmation') ?? '');
    if (this.registering && password !== passwordConfirmation) {
      this.error.set('As senhas devem ser iguais.');
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      if (this.registering) {
        await this.session.register({
          name: String(data.get('name')).trim(),
          email,
          phone: String(data.get('phone')).trim(),
          birthDate: String(data.get('birthDate')),
          cpf: String(data.get('cpf')).trim(),
          password,
          passwordConfirmation,
          address: {
            zipCode: String(data.get('zipCode')).trim(),
            street: String(data.get('street')).trim(),
            number: String(data.get('number')).trim(),
            complement: String(data.get('complement')).trim(),
            district: String(data.get('district')).trim(),
            city: String(data.get('city')).trim(),
            state: String(data.get('state')).trim().toUpperCase(),
          },
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

  protected async lookupZipCode(form: HTMLFormElement, input: HTMLInputElement): Promise<void> {
    if (this.lookingUpZipCode()) return;
    const zipCode = input.value.trim();
    if (!/^[0-9]{5}-?[0-9]{3}$/.test(zipCode)) {
      this.zipCodeStatus.set('Informe um CEP com 8 dígitos.');
      return;
    }

    const addressFields = [
      ['street', 'logradouro'],
      ['district', 'bairro'],
      ['city', 'localidade'],
      ['state', 'uf'],
    ] as const;
    const fields = addressFields.map(([name, responseKey]) => {
      const field = form.elements.namedItem(name) as HTMLInputElement;
      return { field, responseKey, original: field.value };
    });
    this.lookingUpZipCode.set(true);
    this.zipCodeStatus.set('Consultando CEP…');
    try {
      const address = await this.viaCep.lookup(zipCode.replace('-', ''));
      if (input.value.trim() !== zipCode) return;
      if (address.erro) {
        this.zipCodeStatus.set('CEP não encontrado. Confira o número ou preencha o endereço manualmente.');
        return;
      }

      for (const { field, responseKey, original } of fields) {
        if (field.value === original) field.value = address[responseKey] ?? '';
      }
      this.zipCodeStatus.set('Confira o endereço e complete os campos restantes.');
    } catch {
      if (input.value.trim() === zipCode) {
        this.zipCodeStatus.set('Não foi possível consultar o CEP. Você pode preencher o endereço manualmente.');
      }
    } finally {
      this.lookingUpZipCode.set(false);
    }
  }

  protected get registered(): boolean {
    return this.registration?.['registered'] === true;
  }

  private showError(error: unknown): void {
    const response = error instanceof HttpErrorResponse ? error.error : undefined;
    const message = response?.violations?.[0]?.message ?? response?.message;
    this.error.set(typeof message === 'string' ? message : 'Não foi possível concluir. Tente novamente.');
  }
}
