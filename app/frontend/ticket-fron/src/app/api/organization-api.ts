import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { CommercialProfile, CreateOrganization, OrganizationOnboarding } from '../interfaces/organization';

const url = '/v1/api/organizations';

@Injectable({ providedIn: 'root' })
export class OrganizationApi {
  private readonly http = inject(HttpClient);

  list(accessToken: string): Promise<OrganizationOnboarding[]> {
    return firstValueFrom(this.http.get<OrganizationOnboarding[]>(url + '/onboarding', this.options(accessToken)));
  }

  create(accessToken: string, request: CreateOrganization): Promise<{ id: string }> {
    return firstValueFrom(this.http.post<{ id: string }>(url, request, this.options(accessToken)));
  }

  save(accessToken: string, id: string, profile: CommercialProfile): Promise<OrganizationOnboarding> {
    return firstValueFrom(this.http.put<OrganizationOnboarding>(
      `${url}/${encodeURIComponent(id)}/commercial-profile`, profile, this.options(accessToken),
    ));
  }

  private options(accessToken: string) {
    return { headers: { Authorization: 'Bearer ' + accessToken } };
  }
}
