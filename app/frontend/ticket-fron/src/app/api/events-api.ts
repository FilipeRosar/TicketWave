import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { EventDetails, EventPage } from '../interfaces/catalog-event';

const eventsUrl = '/v1/api/event';

@Injectable({ providedIn: 'root' })
export class EventsApi {
  private readonly http = inject(HttpClient);

  list(accessToken: string, page: number): Promise<EventPage> {
    return firstValueFrom(this.http.get<EventPage>(eventsUrl, {
      headers: { Authorization: 'Bearer ' + accessToken },
      params: { page, size: 24, sort: ['startDate,asc', 'id,asc'] },
    }));
  }

  details(accessToken: string, id: string): Promise<EventDetails> {
    return firstValueFrom(this.http.get<EventDetails>(eventsUrl + '/' + encodeURIComponent(id), {
      headers: { Authorization: 'Bearer ' + accessToken },
    }));
  }
}
