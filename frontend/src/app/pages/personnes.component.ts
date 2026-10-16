import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../services/api.service';
import { Utilisateur } from '../models/models';
import { PageState } from './page-state';
@Component({
  selector: 'app-personnes', imports: [ReactiveFormsModule],
  templateUrl: './personnes.component.html'
})
export class PersonnesComponent extends PageState implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  readonly type = this.route.snapshot.data['type'] as 'etudiants' | 'enseignants';
  readonly titre = this.type === 'etudiants' ? 'Étudiants' : 'Enseignants';
  readonly form = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.pattern(/\S/)]],
    prenom: ['', [Validators.required, Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.email]]
  });
  personnes: Utilisateur[] = [];
  edition = false;
  id: number | null = null;
  ngOnInit(): void { void this.load(); }
  load(): Promise<void> { return this.charger(async () => { this.personnes = await firstValueFrom(this.api.personnes(this.type)); }); }
  nouveau(): void { this.id = null; this.form.reset(); this.edition = true; }
  modifier(p: Utilisateur): void { this.id = p.id; this.form.setValue({ nom: p.nom, prenom: p.prenom, email: p.email }); this.edition = true; }
  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    void this.envoyer(async () => {
      const data = this.form.getRawValue();
      await firstValueFrom(this.api.enregistrerPersonne(this.type, { nom: data.nom.trim(), prenom: data.prenom.trim(), email: data.email.trim() }, this.id));
      this.edition = false;
      this.success = 'Utilisateur enregistré.';
      await this.load();
    });
  }
  supprimer(p: Utilisateur): void {
    if (!window.confirm('Supprimer ' + p.prenom + ' ' + p.nom + ' ?')) return;
    void this.envoyer(async () => {
      await firstValueFrom(this.api.supprimerPersonne(this.type, p.id));
      if (this.id === p.id) this.edition = false;
      this.success = 'Utilisateur supprimé.';
      await this.load();
    });
  }
}