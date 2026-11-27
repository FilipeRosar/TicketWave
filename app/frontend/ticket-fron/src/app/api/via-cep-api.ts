import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { ViaCepResponse } from '../interfaces/address';

@Injectable({ providedIn: 'root' })
export class ViaCepApi {
  private readonly http = inject(HttpClient);

  lookup(zipCode: string): Promise<ViaCepResponse> {
    return firstValueFrom(this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${zipCode}/json/`, {
      timeout: 5_000,
    }));
  }
}
