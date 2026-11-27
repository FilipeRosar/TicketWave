import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, computed, DestroyRef, ElementRef, inject, Injector, input, OnInit, signal, viewChild } from '@angular/core';
import { AuthSession } from '../auth/auth-session';
import { connectionGoals, type ConnectionGoal, type ConnectionSuggestion, type ParticipantProfile } from '../interfaces/participant-profile';

@Component({
  selector: 'app-participant-profile',
  templateUrl: './participant-profile-page.html',
  styleUrls: ['./participant-profile-page.css', './profile-cover.css', './profile-preferences.css', './profile-dark.css'],
})
export class ParticipantProfilePage implements OnInit {
  private readonly session = inject(AuthSession);
  private readonly injector = inject(Injector);
  private readonly editorTitle = viewChild<ElementRef<HTMLHeadingElement>>('editorTitle');
  readonly accountName = input.required<string>();
  private suggestionRequest = 0;
  protected readonly goalLabels = connectionGoals;
  protected readonly goals = Object.keys(connectionGoals) as ConnectionGoal[];
  protected readonly suggestions = signal<ConnectionSuggestion[] | null>(null);
  protected readonly suggestionError = signal(false);
  protected readonly profile = signal<ParticipantProfile | null>(null);
  protected readonly loaded = signal(false);
  protected readonly editing = signal(false);
  protected readonly confirmingDelete = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly status = signal('');
  protected readonly photoPreview = signal<string | null>(null);
  protected readonly photoName = signal('');
  protected readonly removingPhoto = signal(false);
  protected readonly displayName = computed(() => this.profile()?.name ?? this.accountName());
  protected readonly initials = computed(() => {
    const names = this.displayName().trim().split(/\s+/);
    const first = Array.from(names[0] ?? '')[0] ?? '';
    const last = names.length > 1 ? Array.from(names[names.length - 1])[0] : '';
    return (first + last).toLocaleUpperCase('pt-BR');
  });
  protected readonly photoUrl = computed(() => {
    const profile = this.profile();
    return profile?.photo && profile.photoType ? `data:${profile.photoType};base64,${profile.photo}` : null;
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => this.clearPhotoPreview());
  }

  ngOnInit(): void {
    void this.loadProfile();
  }

  protected async loadProfile(): Promise<void> {
    this.error.set('');
    try {
      this.profile.set(await this.session.getParticipantProfile());
      this.loaded.set(true);
      void this.loadSuggestions();
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 404) {
        this.loaded.set(true);
      } else {
        this.showError(error);
      }
    }
  }

  protected async loadSuggestions(): Promise<void> {
    const request = ++this.suggestionRequest;
    this.suggestions.set(null);
    this.suggestionError.set(false);
    try {
      const suggestions = await this.session.getConnectionSuggestions();
      if (request === this.suggestionRequest) this.suggestions.set(suggestions);
    } catch {
      if (request === this.suggestionRequest) this.suggestionError.set(true);
    }
  }

  protected startEditing(): void {
    this.error.set('');
    this.status.set('');
    this.editing.set(true);
    afterNextRender(() => this.editorTitle()?.nativeElement.focus(), { injector: this.injector });
  }

  protected cancelEditing(): void {
    this.editing.set(false);
    this.error.set('');
    this.removingPhoto.set(false);
    this.clearPhotoPreview();
  }

  protected selectPhoto(input: HTMLInputElement): void {
    this.clearPhotoPreview();
    this.error.set('');
    const file = input.files?.[0];
    if (!file) return;

    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 1024 * 1024) {
      input.value = '';
      this.error.set('Escolha uma foto JPEG ou PNG de até 1 MB.');
      return;
    }

    this.removingPhoto.set(false);
    this.photoName.set(file.name);
    this.photoPreview.set(URL.createObjectURL(file));
  }

  private clearPhotoPreview(): void {
    const preview = this.photoPreview();
    if (preview) URL.revokeObjectURL(preview);
    this.photoPreview.set(null);
    this.photoName.set('');
  }

  protected async save(event: SubmitEvent, form: HTMLFormElement): Promise<void> {
    event.preventDefault();
    if (this.busy()) return;

    const data = new FormData(form);
    const interests = [...new Set(String(data.get('interestList') ?? '').split(',')
      .map(interest => interest.trim().normalize('NFC').toLocaleLowerCase('pt-BR'))
      .filter(Boolean))];
    if (interests.length > 10 || interests.some(interest => interest.length > 40)) {
      this.error.set('Adicione até 10 interesses com no máximo 40 caracteres cada.');
      return;
    }
    data.delete('interestList');
    for (const interest of interests) data.append('interests', interest);
    const photo = data.get('photo');
    if (photo instanceof File && photo.size > 1024 * 1024) {
      this.error.set('A foto deve ter até 1 MB.');
      return;
    }
    if (photo instanceof File && !photo.size) data.delete('photo');

    this.error.set('');
    this.busy.set(true);
    try {
      this.profile.set(await this.session.saveParticipantProfile(data));
      this.cancelEditing();
      this.status.set('Perfil salvo.');
      void this.loadSuggestions();
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  protected async deleteProfile(): Promise<void> {
    if (this.busy()) return;
    this.error.set('');
    this.status.set('');
    this.busy.set(true);
    try {
      await this.session.deleteParticipantProfile();
      this.profile.set(null);
      this.suggestionRequest++;
      this.suggestions.set(null);
      this.confirmingDelete.set(false);
      this.status.set('Perfil excluído.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  private showError(error: unknown): void {
    const response = error instanceof HttpErrorResponse ? error.error : undefined;
    this.error.set(response?.violations?.[0]?.message ?? response?.message ?? 'Não foi possível acessar o perfil. Tente novamente.');
  }
}
