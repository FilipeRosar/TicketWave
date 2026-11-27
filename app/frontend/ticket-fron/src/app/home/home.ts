import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ThemeToggle } from '../theme/theme-toggle';
import { AuthSession } from '../auth/auth-session';
import type { CatalogEvent, EventDetails, EventSummary, NetworkingFeature } from '../interfaces/catalog-event';

const previewEvents: readonly CatalogEvent[] = [
  {
    title: 'Sunset Sessions',
    category: 'Música',
    date: 'SÁB, 12 DE DEZ · 17H',
    location: 'São Paulo, SP',
    price: 'R$ 89',
    image: 'assets/home/photo-1.webp',
    description: 'Uma tarde de música eletrônica ao ar livre, do pôr do sol até a noite.',
  },
  {
    title: 'Future Tech Summit',
    category: 'Tecnologia',
    date: 'SEX, 18 DE DEZ · 09H',
    location: 'São Paulo, SP',
    price: 'R$ 120',
    image: 'assets/home/photo-2.webp',
    description: 'Um encontro para trocar ideias sobre tecnologia, design e o futuro dos produtos digitais.',
  },
  {
    title: 'Noite em Movimento',
    category: 'Arte e cultura',
    date: 'SÁB, 19 DE DEZ · 20H',
    location: 'Rio de Janeiro, RJ',
    price: 'R$ 65',
    image: 'assets/home/photo-3.webp',
    description: 'Um espetáculo de dança contemporânea que explora ritmo, luz e movimento.',
  },
];

const dateFormat = new Intl.DateTimeFormat('pt-BR', {
  weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
});
const priceFormat = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

function eventCard(event: EventSummary): CatalogEvent {
  return {
    id: event.id,
    classification: event.classification,
    title: event.title,
    category: event.category,
    date: dateFormat.format(new Date(event.startDate)),
    location: event.city,
    price: priceFormat.format(event.ticketPrice),
    image: 'assets/home/festival-day.webp',
    description: event.shortDescription,
  };
}

function normalize(text: string): string {
  return text.normalize('NFD').replace(/\p{M}/gu, '').toLocaleLowerCase('pt-BR');
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, ThemeToggle],
  styleUrls: ['./home.css', './home-dark.css', './event-networking.css'],
  templateUrl: './home.html',
})
export class Home {
  protected readonly session = inject(AuthSession);
  private readonly publishedEvents = signal<CatalogEvent[]>([]);
  private nextPage = 0;
  private catalogRequest = 0;
  private detailRequest = 0;
  protected readonly loadingEvents = signal(false);
  protected readonly loadingDetails = signal(false);
  protected readonly catalogError = signal(false);
  protected readonly detailError = signal(false);
  protected readonly hasMore = signal(false);
  protected readonly selectedDetails = signal<EventDetails | null>(null);
  protected readonly featureNames: Record<NetworkingFeature, string> = {
    MEETING_AGENDA: 'Agenda de reuniões',
    ROOMS: 'Salas',
    WALL: 'Mural',
    GROUPS: 'Grupos',
  };
  protected readonly classificationNames = { PROFESSIONAL: 'Profissional', SOCIAL: 'Social' };
  private readonly events = computed(() => this.session.user() ? this.publishedEvents() : previewEvents);
  protected readonly categories = computed(() => ['Todos', ...new Set(this.events().map(event => event.category))]);
  protected readonly category = signal('Todos');
  protected readonly cities = computed(() => [...new Set(this.events().map(event => event.location))]);
  protected readonly city = signal('');
  protected readonly query = signal('');
  protected readonly selectedEvent = signal<CatalogEvent | null>(null);
  protected readonly filteredEvents = computed(() => {
    const query = normalize(this.query().trim());
    return this.events().filter((event) =>
      (this.category() === 'Todos' || event.category === this.category()) &&
      (!this.city() || event.location === this.city()) &&
      normalize(`${event.title} ${event.category} ${event.location}`).includes(query),
    );
  });

  constructor() {
    effect(() => {
      const user = this.session.user();
      untracked(() => {
        this.catalogRequest++;
        this.loadingEvents.set(false);
        this.catalogError.set(false);
        this.hasMore.set(false);
        this.nextPage = 0;
        this.publishedEvents.set([]);
        if (user) void this.loadEvents();
      });
    });
  }

  protected async loadEvents(): Promise<void> {
    if (this.loadingEvents()) return;
    const request = ++this.catalogRequest;
    this.loadingEvents.set(true);
    this.catalogError.set(false);
    try {
      const page = await this.session.listPublishedEvents(this.nextPage);
      if (request !== this.catalogRequest) return;
      this.publishedEvents.update(events => [...events, ...page.content.map(eventCard)]);
      this.nextPage++;
      this.hasMore.set(!page.last);
    } catch {
      if (request === this.catalogRequest) this.catalogError.set(true);
    } finally {
      if (request === this.catalogRequest) this.loadingEvents.set(false);
    }
  }

  protected search(event: SubmitEvent, value: string, city: string, category: string, catalog: HTMLElement): void {
    event.preventDefault();
    this.query.set(value);
    this.city.set(city);
    this.category.set(category);
    catalog.scrollIntoView({ block: 'start' });
  }

  protected showEvent(event: CatalogEvent, dialog: HTMLDialogElement): void {
    this.selectedEvent.set(event);
    this.selectedDetails.set(null);
    this.detailError.set(false);
    dialog.showModal();
    if (event.id) void this.loadDetails(event.id);
  }

  protected async loadDetails(id: string): Promise<void> {
    const request = ++this.detailRequest;
    this.loadingDetails.set(true);
    this.detailError.set(false);
    try {
      const details = await this.session.getEventDetails(id);
      if (request === this.detailRequest) this.selectedDetails.set(details);
    } catch {
      if (request === this.detailRequest) this.detailError.set(true);
    } finally {
      if (request === this.detailRequest) this.loadingDetails.set(false);
    }
  }

  protected clearFilters(searchInput: HTMLInputElement, locationInput: HTMLSelectElement, categoryInput: HTMLSelectElement): void {
    this.category.set('Todos');
    this.query.set('');
    this.city.set('');
    searchInput.value = '';
    locationInput.value = '';
    categoryInput.value = 'Todos';
  }
}
