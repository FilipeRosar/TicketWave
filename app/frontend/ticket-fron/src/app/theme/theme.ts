import { computed, inject, Injectable, signal } from '@angular/core';
import { AuthSession } from '../auth/auth-session';

const preferenceKey = 'ticketwave.theme';

@Injectable({ providedIn: 'root' })
export class Theme {
  private readonly session = inject(AuthSession);
  private readonly preference = signal(this.readPreference());
  readonly dark = computed(() => !!this.session.user() && this.preference());

  toggle(): void {
    if (!this.session.user()) return;
    const dark = !this.preference();
    this.preference.set(dark);
    try {
      localStorage.setItem(preferenceKey, dark ? 'dark' : 'light');
    } catch {
      // The current session can still use the theme when storage is unavailable.
    }
  }

  private readPreference(): boolean {
    try {
      return localStorage.getItem(preferenceKey) === 'dark';
    } catch {
      return false;
    }
  }
}
