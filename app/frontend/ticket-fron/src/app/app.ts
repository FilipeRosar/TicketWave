import { Component, computed, signal } from '@angular/core';

const events = [
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
] as const;

type CatalogEvent = (typeof events)[number];

function normalize(text: string): string {
  return text.normalize('NFD').replace(/\p{M}/gu, '').toLocaleLowerCase('pt-BR');
}

@Component({
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly categories = ['Todos', 'Música', 'Tecnologia', 'Arte e cultura'];
  protected readonly category = signal('Todos');
  protected readonly query = signal('');
  protected readonly selectedEvent = signal<CatalogEvent | null>(null);
  protected readonly filteredEvents = computed(() => {
    const query = normalize(this.query().trim());
    return events.filter((event) =>
      (this.category() === 'Todos' || event.category === this.category()) &&
      normalize(`${event.title} ${event.category} ${event.location}`).includes(query),
    );
  });

  protected search(event: SubmitEvent, value: string, catalog: HTMLElement): void {
    event.preventDefault();
    this.query.set(value);
    catalog.scrollIntoView({ block: 'start' });
  }

  protected showEvent(event: CatalogEvent, dialog: HTMLDialogElement): void {
    this.selectedEvent.set(event);
    dialog.showModal();
  }

  protected clearFilters(searchInput: HTMLInputElement): void {
    this.category.set('Todos');
    this.query.set('');
    searchInput.value = '';
  }
}
