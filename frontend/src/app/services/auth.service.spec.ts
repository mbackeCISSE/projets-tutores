import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthService } from './auth.service';
import { SessionService } from './session.service';
import { MOCK_ENABLED } from './app-settings';
import { mockApiInterceptor } from './mock-api.service';
import { API_URL, utilisateurInterceptor } from './api.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

describe('Connexion avec les comptes sénégalais', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [
    { provide: MOCK_ENABLED, useValue: true },
    provideHttpClient(withInterceptors([utilisateurInterceptor, mockApiInterceptor]))
  ] }));

  it('connecte un enseignant et conserve son rôle en mémoire', async () => {
    await TestBed.inject(AuthService).connexion(' MOUSSA.DIOP@gdil.example ', 'Projet2026!');
    expect(TestBed.inject(SessionService).utilisateur()?.prenom).toBe('Moussa');
    expect(TestBed.inject(SessionService).utilisateur()?.role).toBe('ENSEIGNANT');
  });
  it('refuse un mauvais mot de passe sans créer de session', async () => {
    await expectAsync(TestBed.inject(AuthService).connexion('aminata.diallo@gdil.example', 'incorrect')).toBeRejectedWith(jasmine.objectContaining({ status: 401 }));
    expect(TestBed.inject(SessionService).utilisateur()).toBeNull();
  });
  it('refuse un email inconnu', async () => {
    await expectAsync(TestBed.inject(AuthService).connexion('inconnu@gdil.example', 'Projet2026!')).toBeRejectedWith(jasmine.objectContaining({ status: 401 }));
  });
});

describe('Connexion au backend Spring Boot', () => {
  let http: HttpTestingController;
  let auth: AuthService;
  let session: SessionService;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([utilisateurInterceptor])), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    session = TestBed.inject(SessionService);
  });
  afterEach(() => http.verify());
  it('envoie les identifiants sans en-tête et utilise l’identifiant renvoyé par le backend', async () => {
    const attente = auth.connexion(' AMINATA.DIALLO@gdil.example ', ' Projet2026! ');
    const request = http.expectOne(API_URL + '/auth/connexion');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'aminata.diallo@gdil.example', motDePasse: ' Projet2026! ' });
    expect(request.request.headers.has('X-Utilisateur-Id')).toBeFalse();
    request.flush({ id: 814, nom: 'Diallo', prenom: 'Aminata', email: 'aminata.diallo@gdil.example', role: 'ETUDIANT' });
    await attente;
    expect(session.utilisateur()?.id).toBe(814);
    http.expectNone(API_URL + '/utilisateurs-demo');
  });
  it('efface l’ancienne session et transmet un refus 401 sans basculer sur les mocks', async () => {
    session.utilisateur.set({ id: 12, nom: 'Test', prenom: 'Profil', email: 'test@example.fr', role: 'ADMIN' });
    const attente = auth.connexion('inconnu@gdil.example', 'incorrect');
    const verification = expectAsync(attente).toBeRejectedWith(jasmine.objectContaining({ status: 401 }));
    http.expectOne(API_URL + '/auth/connexion').flush({ message: 'Adresse email ou mot de passe incorrect.' }, { status: 401, statusText: 'Unauthorized' });
    await verification;
    expect(session.utilisateur()).toBeNull();
    http.expectNone(API_URL + '/utilisateurs-demo');
  });
});
