import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiService, utilisateurInterceptor } from './api.service';
import { MOCK_ENABLED } from './app-settings';
import { mockApiInterceptor } from './mock-api.service';
import { SessionService } from './session.service';

describe('API en mémoire', () => {
  let api: ApiService;
  let session: SessionService;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [
      { provide: MOCK_ENABLED, useValue: true },
      provideHttpClient(withInterceptors([utilisateurInterceptor, mockApiInterceptor]))
    ] });
    api = TestBed.inject(ApiService);
    session = TestBed.inject(SessionService);
  });
  async function choisir(id: number): Promise<void> {
    const utilisateurs = await firstValueFrom(api.utilisateurs());
    session.utilisateur.set(utilisateurs.find(u => u.id === id)!);
  }
  it('permet le parcours sujet, candidature, décision et suivi entre profils', async () => {
    await choisir(2);
    const sujet = await firstValueFrom(api.enregistrerSujet({ titre: 'Projet de test', description: 'Description', capaciteMax: 1, ouvert: true }, null));
    expect(sujet.enseignantId).toBe(2);
    await choisir(4);
    const candidature = await firstValueFrom(api.candidater(sujet.id, 'Ma motivation'));
    expect(candidature.etudiantId).toBe(4);
    await choisir(7);
    await firstValueFrom(api.decider(candidature.id, 'ACCEPTEE'));
    const sujets = await firstValueFrom(api.sujets());
    expect(sujets.find(s => s.id === sujet.id)?.placesRestantes).toBe(0);
    await choisir(4);
    const candidatures = await firstValueFrom(api.candidatures());
    expect(candidatures.find(c => c.id === candidature.id)?.statut).toBe('ACCEPTEE');
    expect((await firstValueFrom(api.sujets())).some(s => s.id === sujet.id)).toBeFalse();
  });
  it('interdit une nouvelle candidature sur un sujet déjà rejeté', async () => {
    await choisir(4);
    await expectAsync(firstValueFrom(api.candidater(3, 'Nouvelle motivation'))).toBeRejectedWith(jasmine.objectContaining({ status: 409 }));
  });
  it('respecte les rôles et la propriété des sujets', async () => {
    await choisir(2);
    expect((await firstValueFrom(api.sujets())).every(s => s.enseignantId === 2)).toBeTrue();
    await expectAsync(firstValueFrom(api.supprimerSujet(2))).toBeRejectedWith(jasmine.objectContaining({ status: 403 }));
    await choisir(1);
    await expectAsync(firstValueFrom(api.candidatures())).toBeRejectedWith(jasmine.objectContaining({ status: 403 }));
  });
  it('permet l’ajout, la modification et la suppression d’un étudiant sans liens', async () => {
    await choisir(1);
    const personne = await firstValueFrom(api.enregistrerPersonne('etudiants', { nom: 'Test', prenom: 'Étudiant', email: 'test@demo.fr' }, null));
    const modifie = await firstValueFrom(api.enregistrerPersonne('etudiants', { nom: 'Modifié', prenom: personne.prenom, email: personne.email }, personne.id));
    expect(modifie.nom).toBe('Modifié');
    await firstValueFrom(api.supprimerPersonne('etudiants', personne.id));
    expect((await firstValueFrom(api.personnes('etudiants'))).some(p => p.id === personne.id)).toBeFalse();
  });
});
