import { Component, inject } from '@angular/core';
import { Theme } from './theme';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <button type="button" aria-label="Modo escuro" [attr.aria-pressed]="theme.dark()" (click)="theme.toggle()">
      <span class="symbol" aria-hidden="true">{{ theme.dark() ? '☀' : '☾' }}</span>
      <span class="label">Modo escuro</span>
    </button>
  `,
  styles: `
    button { display: flex; align-items: center; justify-content: center; gap: 6px; min-height: 40px;
      padding: 6px 10px; border: 1px solid var(--wave-line); border-radius: 10px;
      background: var(--wave-surface); color: var(--wave-ink); cursor: pointer; font-size: .8rem;
      font-weight: 700; white-space: nowrap; }
    button:hover, button[aria-pressed="true"] { background: var(--wave-mist); }
    .symbol { font-size: 1.1rem; line-height: 1; }
    @media (max-width: 1100px) { .label { display: none; } button { width: 40px; } }
  `,
})
export class ThemeToggle {
  protected readonly theme = inject(Theme);
}
