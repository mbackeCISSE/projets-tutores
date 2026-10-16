import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { roles } from '../../models/models';

@Component({
  selector: 'app-header', imports: [RouterLink], templateUrl: './header.component.html'
})
export class HeaderComponent {
  readonly session = inject(SessionService);
  readonly roles = roles;
  private readonly router = inject(Router);
  deconnecter(): void { this.session.utilisateur.set(null); void this.router.navigateByUrl('/connexion'); }
}
