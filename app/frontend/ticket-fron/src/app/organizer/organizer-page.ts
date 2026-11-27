import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthSession } from '../auth/auth-session';
import type { OnboardingPending, OrganizationOnboarding, OrganizationType, VerificationReason, VerificationStatus } from '../interfaces/organization';
import { ThemeToggle } from '../theme/theme-toggle';

@Component({
  selector: 'app-organizer-page',
  imports: [RouterLink, ThemeToggle],
  templateUrl: './organizer-page.html',
  styleUrl: './organizer-page.css',
})
export class OrganizerPage implements OnInit {
  protected readonly session = inject(AuthSession);
  protected readonly organizations = signal<OrganizationOnboarding[]>([]);
  protected readonly selectedId = signal<string | null>(null);
  protected readonly selected = computed(() => this.organizations().find(organization => organization.id === this.selectedId()));
  protected readonly type = signal<OrganizationType>('PF');
  protected readonly creating = signal(false);
  protected readonly loaded = signal(false);
  protected readonly busy = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected readonly pendingLabels: Record<OnboardingPending, string> = {
    COMMERCIAL_NAME: 'Preencher o nome comercial',
    CONTACT_EMAIL: 'Informar o e-mail de contato',
    FINANCIAL_APPROVAL: 'Aguardar a habilitação financeira',
  };

  protected readonly verificationLabels: Record<VerificationStatus, string> = {
    PENDING: 'Pendente',
    IN_REVIEW: 'Em análise',
    APPROVED: 'Aprovada',
    REJECTED: 'Rejeitada',
  };
  protected readonly verificationReasons: Record<VerificationReason, string> = {
    INVALID_DOCUMENT: 'O documento informado precisa ser corrigido.',
    DATA_MISMATCH: 'Os dados cadastrais precisam ser revisados.',
    INSUFFICIENT_EVIDENCE: 'Os dados disponíveis foram insuficientes para a análise.',
  };

  ngOnInit(): void {
    if (this.session.user()) void this.load();
  }

  protected async load(selectedId = this.selectedId()): Promise<void> {
    this.error.set('');
    this.loaded.set(false);
    try {
      const organizations = await this.session.listOrganizations();
      this.organizations.set(organizations);
      this.selectedId.set(organizations.find(organization => organization.id === selectedId)?.id
        ?? organizations[0]?.id ?? null);
      this.creating.set(!organizations.length);
      this.loaded.set(true);
    } catch (error) {
      this.showError(error);
    }
  }

  protected select(id: string): void {
    this.selectedId.set(id);
    this.creating.set(false);
    this.error.set('');
    this.message.set('');
  }

  protected startCreating(): void {
    this.type.set('PF');
    this.creating.set(true);
    this.error.set('');
    this.message.set('');
  }

  protected async register(event: SubmitEvent, form: HTMLFormElement): Promise<void> {
    event.preventDefault();
    if (this.busy()) return;
    const data = new FormData(form);
    const document = String(data.get('document') ?? '').trim();
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    try {
      const organization = await this.session.createOrganization({
        name: String(data.get('name') ?? '').trim(), type: this.type(), document: document || undefined,
      });
      this.selectedId.set(organization.id);
      await this.load(organization.id);
      this.message.set('Organização cadastrada.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  protected async save(event: SubmitEvent, form: HTMLFormElement, id: string): Promise<void> {
    event.preventDefault();
    if (this.busy()) return;
    const data = new FormData(form);
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    try {
      const updated = await this.session.saveCommercialProfile(id, {
        commercialName: this.field(data, 'commercialName'),
        contactEmail: this.field(data, 'contactEmail'),
        description: this.field(data, 'description'),
      });
      this.organizations.update(items => items.map(item => item.id === id ? updated : item));
      this.message.set('Perfil comercial salvo.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  protected async submitVerification(event: SubmitEvent, form: HTMLFormElement, id: string): Promise<void> {
    event.preventDefault();
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    try {
      const result = await this.session.submitOrganizationVerification(id, this.field(new FormData(form), 'document') ?? undefined);
      this.organizations.update(items => items.map(item => item.id === id ? {
        ...item,
        verificationStatus: result.status,
        verificationReason: result.reason,
        documentAvailable: result.documentAvailable,
      } : item));
      this.message.set('Verificação enviada para análise.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.busy.set(false);
    }
  }

  private field(data: FormData, name: string): string | null {
    return String(data.get(name) ?? '').trim() || null;
  }

  private showError(error: unknown): void {
    const response = error instanceof HttpErrorResponse ? error.error : undefined;
    this.error.set(response?.violations?.[0]?.message ?? response?.message ?? 'Não foi possível concluir. Tente novamente.');
  }
}
