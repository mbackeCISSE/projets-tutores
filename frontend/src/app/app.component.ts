import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionService } from './services/session.service';
import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
@Component({ selector: 'app-root', imports: [RouterOutlet, HeaderComponent, SidebarComponent], templateUrl: './app.component.html' })
export class AppComponent {
  readonly session = inject(SessionService);
}
