import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ThemeToggle } from '../theme/theme-toggle';
import { AuthSession } from '../auth/auth-session';
import type { CatalogEvent } from '../interfaces/catalog-event';

const events: readonly CatalogEvent[] = [
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

function normalize(text: string): string {
  return text.normalize('NFD').replace(/\p{M}/gu, '').toLocaleLowerCase('pt-BR');
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, ThemeToggle],
  styleUrls: ['./home.css', './home-dark.css'],
  templateUrl: './home.html',
})
export class Home {
  protected readonly session = inject(AuthSession);
  protected readonly categories = ['Todos', 'Música', 'Tecnologia', 'Arte e cultura'];
  protected readonly category = signal('Todos');
  protected readonly cities = [...new Set(events.map((event) => event.location))];
  protected readonly city = signal('');
  protected readonly query = signal('');
  protected readonly selectedEvent = signal<CatalogEvent | null>(null);
  protected readonly filteredEvents = computed(() => {
    const query = normalize(this.query().trim());
    return events.filter((event) =>
      (this.category() === 'Todos' || event.category === this.category()) &&
      (!this.city() || event.location === this.city()) &&
      normalize(`${event.title} ${event.category} ${event.location}`).includes(query),
    );
  });

  protected search(event: SubmitEvent, value: string, city: string, category: string, catalog: HTMLElement): void {
    event.preventDefault();
    this.query.set(value);
    this.city.set(city);
    this.category.set(category);
    catalog.scrollIntoView({ block: 'start' });
  }

  protected showEvent(event: CatalogEvent, dialog: HTMLDialogElement): void {
    this.selectedEvent.set(event);
    dialog.showModal();
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
