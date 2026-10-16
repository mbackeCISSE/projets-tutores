import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { SessionService } from '../services/session.service';
import { PageState } from './page-state';

@Component({ selector: 'app-connexion', imports: [ReactiveFormsModule], templateUrl: './connexion.component.html' })
export class ConnexionComponent extends PageState {
  private readonly auth = inject(AuthService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  readonly form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required]
  });

  connecter(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    void this.envoyer(async () => {
      const { email, motDePasse } = this.form.getRawValue();
      await this.auth.connexion(email, motDePasse);
      await this.router.navigateByUrl(this.session.utilisateur()?.role === 'ADMIN' ? '/etudiants' : '/sujets');
    });
  }
}
