import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom, forkJoin, of } from 'rxjs';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';
import { Candidature, Statut, statuts, Sujet } from '../models/models';
import { PageState } from './page-state';
@Component({
  selector: 'app-candidatures', imports: [DatePipe], templateUrl: './candidatures.component.html'
})
export class CandidaturesComponent extends PageState implements OnInit {
  private readonly api = inject(ApiService);
  readonly role = inject(SessionService).utilisateur()!.role;
  readonly affectations = inject(ActivatedRoute).snapshot.data['affectations'] === true;
  readonly statuts = statuts;
  candidatures: Candidature[] = [];
  sujets: Sujet[] = [];
  get visibles(): Candidature[] { return this.affectations ? this.candidatures.filter(c => c.statut === 'ACCEPTEE') : this.candidatures; }
  ngOnInit(): void { void this.load(); }
  load(): Promise<void> {
    return this.charger(async () => {
      const result = await firstValueFrom(forkJoin({
        candidatures: this.api.candidatures(),
        sujets: this.role === 'RESPONSABLE' ? this.api.sujets() : of([] as Sujet[])
      }));
      this.candidatures = result.candidatures;
      this.sujets = result.sujets;
    });
  }
  places(id: number): number | undefined { return this.sujets.find(s => s.id === id)?.placesRestantes; }
  decider(c: Candidature, statut: Exclude<Statut, 'EN_ATTENTE'>): void {
    if (c.statut !== 'EN_ATTENTE') return;
    void this.envoyer(async () => {
      await firstValueFrom(this.api.decider(c.id, statut));
      this.success = statut === 'ACCEPTEE' ? 'Candidature acceptée.' : 'Candidature rejetée.';
      await this.load();
    });
  }
}