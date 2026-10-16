import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom, forkJoin, of } from 'rxjs';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';
import { Candidature, Sujet } from '../models/models';
import { PageState } from './page-state';
import { SubjectCardComponent } from '../components/subject-card/subject-card.component';
@Component({
  selector: 'app-sujets', imports: [ReactiveFormsModule, SubjectCardComponent], templateUrl: './sujets.component.html'
})
export class SujetsComponent extends PageState implements OnInit {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  readonly role = inject(SessionService).utilisateur()!.role;
  readonly form = this.fb.nonNullable.group({
    titre: ['', [Validators.required, Validators.pattern(/\S/)]],
    description: ['', [Validators.required, Validators.pattern(/\S/)]],
    capaciteMax: [1, [Validators.required, Validators.min(1), Validators.pattern(/^[0-9]+$/)]],
    ouvert: [true]
  });
  readonly candidatureForm = this.fb.nonNullable.group({ motivation: ['', [Validators.required, Validators.pattern(/\S/)]] });
  sujets: Sujet[] = [];
  candidatures: Candidature[] = [];
  edition = false;
  id: number | null = null;
  sujetChoisi: Sujet | null = null;
  ngOnInit(): void { void this.load(); }
  load(): Promise<void> {
    return this.charger(async () => {
      const result = await firstValueFrom(forkJoin({
        sujets: this.api.sujets(),
        candidatures: this.role === 'ETUDIANT' ? this.api.candidatures() : of([] as Candidature[])
      }));
      this.sujets = result.sujets;
      this.candidatures = result.candidatures;
    });
  }
  dejaCandidate(id: number): boolean { return this.candidatures.some(c => c.sujetId === id); }
  nouveau(): void { this.id = null; this.form.reset({ titre: '', description: '', capaciteMax: 1, ouvert: true }); this.edition = true; }
  modifier(sujet: Sujet): void {
    this.id = sujet.id;
    this.form.setValue({ titre: sujet.titre, description: sujet.description, capaciteMax: sujet.capaciteMax, ouvert: sujet.ouvert });
    this.edition = true;
  }
  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    void this.envoyer(async () => {
      const data = this.form.getRawValue();
      await firstValueFrom(this.api.enregistrerSujet({ ...data, titre: data.titre.trim(), description: data.description.trim() }, this.id));
      this.edition = false;
      this.success = 'Sujet enregistré.';
      await this.load();
    });
  }
  supprimer(sujet: Sujet): void {
    if (!window.confirm('Supprimer le sujet « ' + sujet.titre + ' » ?')) return;
    void this.envoyer(async () => {
      await firstValueFrom(this.api.supprimerSujet(sujet.id));
      if (this.id === sujet.id) this.edition = false;
      this.success = 'Sujet supprimé.';
      await this.load();
    });
  }
  choisir(sujet: Sujet): void { this.sujetChoisi = sujet; this.candidatureForm.reset(); }
  candidater(): void {
    this.candidatureForm.markAllAsTouched();
    if (this.candidatureForm.invalid || !this.sujetChoisi || this.dejaCandidate(this.sujetChoisi.id)) return;
    const id = this.sujetChoisi.id;
    void this.envoyer(async () => {
      await firstValueFrom(this.api.candidater(id, this.candidatureForm.getRawValue().motivation.trim()));
      this.sujetChoisi = null;
      this.success = 'Candidature envoyée. Consultez son statut dans « Mes candidatures ».';
      await this.load();
    });
  }
}
