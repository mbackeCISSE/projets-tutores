import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService, API_URL, messageErreur, utilisateurInterceptor } from './api.service';
import { SessionService } from './session.service';
describe('Contrat API', () => {
  let api: ApiService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([utilisateurInterceptor])), provideHttpClientTesting()] });
    api = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
    TestBed.inject(SessionService).utilisateur.set({ id: 12, nom: 'Test', prenom: 'Profil', email: 'test@example.fr', role: 'ETUDIANT' });
  });
  afterEach(() => http.verify());
  it('charge les profils sans en-tête et ajoute le profil aux requêtes API', () => {
    api.utilisateurs().subscribe();
    const demo = http.expectOne(API_URL + '/utilisateurs-demo');
    expect(demo.request.headers.has('X-Utilisateur-Id')).toBeFalse();
    demo.flush([]);
    api.sujets().subscribe();
    const sujets = http.expectOne(API_URL + '/sujets');
    expect(sujets.request.headers.get('X-Utilisateur-Id')).toBe('12');
    sujets.flush([]);
  });
  it('ne transmet pas le profil à une autre origine', () => {
    TestBed.inject(HttpClient).get('https://example.org/api/sujets').subscribe();
    const request = http.expectOne('https://example.org/api/sujets');
    expect(request.request.headers.has('X-Utilisateur-Id')).toBeFalse();
    request.flush([]);
  });
  it('envoie uniquement le sujet et la motivation pour candidater', () => {
    api.candidater(3, 'Ma motivation').subscribe();
    const request = http.expectOne(API_URL + '/candidatures');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ sujetId: 3, motivation: 'Ma motivation' });
    request.flush({}, { status: 201, statusText: 'Created' });
  });
  it('utilise PATCH avec le seul statut pour une décision', () => {
    api.decider(9, 'ACCEPTEE').subscribe();
    const request = http.expectOne(API_URL + '/candidatures/9/statut');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ statut: 'ACCEPTEE' });
    request.flush({});
  });
  it('affiche le message métier envoyé par le backend', () => {
    let message = '';
    api.candidater(3, 'Motivation').subscribe({ error: (error: unknown) => { message = messageErreur(error); } });
    http.expectOne(API_URL + '/candidatures').flush({ message: 'Vous avez déjà candidaté.' }, { status: 409, statusText: 'Conflict' });
    expect(message).toBe('Vous avez déjà candidaté.');
  });
});
