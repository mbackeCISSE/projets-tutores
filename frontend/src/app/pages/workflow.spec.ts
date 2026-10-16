import { fakeAsync, flushMicrotasks, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CandidaturesComponent } from './candidatures.component';
import { SujetsComponent } from './sujets.component';
import { SessionService } from '../services/session.service';
import { API_URL } from '../services/api.service';
import { Candidature } from '../models/models';
import { roleGuard } from '../guards/role.guard';
describe('Règles de candidature et accès', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } }]
  }));
  it('refuse une capacité nulle ou fractionnaire et une motivation vide', () => {
    TestBed.inject(SessionService).utilisateur.set({ id: 1, nom: 'Test', prenom: 'Prof', email: 'p@example.fr', role: 'ENSEIGNANT' });
    const page = TestBed.runInInjectionContext(() => new SujetsComponent());
    page.form.setValue({ titre: 'Sujet', description: 'Description', capaciteMax: 1.5, ouvert: true });
    expect(page.form.invalid).toBeTrue();
    page.form.controls.capaciteMax.setValue(0);
    expect(page.form.invalid).toBeTrue();
    page.form.controls.capaciteMax.setValue(1);
    expect(page.form.valid).toBeTrue();
    page.candidatureForm.controls.motivation.setValue('   ');
    expect(page.candidatureForm.invalid).toBeTrue();
  });
  it('reconnaît une candidature même rejetée', () => {
    TestBed.inject(SessionService).utilisateur.set({ id: 1, nom: 'Test', prenom: 'Étudiant', email: 'e@example.fr', role: 'ETUDIANT' });
    const page = TestBed.runInInjectionContext(() => new SujetsComponent());
    page.candidatures = [{ id: 2, sujetId: 3, etudiantId: 1, etudiantNom: 'Test', sujetTitre: 'Sujet', motivation: 'Texte', dateCandidature: '2026-10-15T10:00:00Z', statut: 'REJETEE' }];
    expect(page.dejaCandidate(3)).toBeTrue();
  });
  it('redirige sans profil et interdit les routes réservées', () => {
    const route = { data: { roles: ['ADMIN'] } } as unknown as Parameters<typeof roleGuard>[0];
    const state = {} as Parameters<typeof roleGuard>[1];
    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => roleGuard(route, state));
    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/connexion');
    TestBed.inject(SessionService).utilisateur.set({ id: 1, nom: 'Test', prenom: 'Étudiant', email: 'e@example.fr', role: 'ETUDIANT' });
    const refus = TestBed.runInInjectionContext(() => roleGuard(route, state));
    expect(router.serializeUrl(refus as ReturnType<Router['createUrlTree']>)).toBe('/sujets');
  });
  it('recharge les candidatures et les places après une décision', fakeAsync(() => {
    TestBed.inject(SessionService).utilisateur.set({ id: 4, nom: 'Test', prenom: 'Responsable', email: 'r@example.fr', role: 'RESPONSABLE' });
    const page = TestBed.runInInjectionContext(() => new CandidaturesComponent());
    const http = TestBed.inject(HttpTestingController);
    const candidature: Candidature = { id: 9, sujetId: 3, etudiantId: 1, etudiantNom: 'Test', sujetTitre: 'Sujet', motivation: 'Texte', dateCandidature: '2026-10-15T10:00:00Z', statut: 'EN_ATTENTE' };
    page.decider(candidature, 'ACCEPTEE');
    expect(page.busy).toBeTrue();
    http.expectOne(API_URL + '/candidatures/9/statut').flush({ ...candidature, statut: 'ACCEPTEE' });
    flushMicrotasks();
    http.expectOne(API_URL + '/candidatures').flush([{ ...candidature, statut: 'ACCEPTEE' }]);
    http.expectOne(API_URL + '/sujets').flush([{ id: 3, placesRestantes: 0 }]);
    flushMicrotasks();
    expect(page.candidatures[0].statut).toBe('ACCEPTEE');
    expect(page.places(3)).toBe(0);
    expect(page.busy).toBeFalse();
    http.verify();
  }));
});
