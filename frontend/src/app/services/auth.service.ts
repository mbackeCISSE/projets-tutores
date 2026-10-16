import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);

  async connexion(email: string, motDePasse: string): Promise<void> {
    this.session.utilisateur.set(null);
    const utilisateur = await firstValueFrom(this.api.connexion(email.trim().toLowerCase(), motDePasse));
    this.session.utilisateur.set(utilisateur);
  }
}
