import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { roles } from '../../models/models';
import { IconComponent } from '../icon/icon.component';

@Component({ selector: 'app-sidebar', imports: [RouterLink, RouterLinkActive, IconComponent], templateUrl: './sidebar.component.html' })
export class SidebarComponent {
  readonly session = inject(SessionService);
  readonly roles = roles;
}
