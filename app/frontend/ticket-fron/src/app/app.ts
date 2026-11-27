import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Theme } from './theme/theme';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
  host: { '[attr.data-theme]': "theme.dark() ? 'dark' : null" },
})
export class App {
  protected readonly theme = inject(Theme);
}
