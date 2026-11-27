import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { ParticipantProfile } from '../interfaces/participant-profile';

const profileUrl = '/v1/api/profile/me';

@Injectable({ providedIn: 'root' })
export class ParticipantProfileApi {
  private readonly http = inject(HttpClient);

  get(accessToken: string): Promise<ParticipantProfile> {
    return firstValueFrom(this.http.get<ParticipantProfile>(profileUrl, this.authorization(accessToken)));
  }

  save(accessToken: string, data: FormData): Promise<ParticipantProfile> {
    return firstValueFrom(this.http.put<ParticipantProfile>(profileUrl, data, this.authorization(accessToken)));
  }

  delete(accessToken: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(profileUrl, this.authorization(accessToken)));
  }

  private authorization(accessToken: string) {
    return { headers: { Authorization: 'Bearer ' + accessToken } };
  }
}
