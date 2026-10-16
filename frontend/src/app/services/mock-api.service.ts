import { inject, Injectable } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { defer, delay, of } from 'rxjs';
import { Candidature, PersonneForm, Role, Statut, Sujet, SujetForm, Utilisateur } from '../models/models';
import { API_URL } from './api.service';
import { MOCK_ENABLED } from './app-settings';
import { candidaturesDemo, MockSujet, MOT_DE_PASSE_MOCK, sujetsDemo, utilisateursDemo } from './mock-data';

export const mockApiInterceptor: HttpInterceptorFn = (request, next) => {
  if (!inject(MOCK_ENABLED) || !request.url.startsWith(API_URL + '/')) return next(request);
  const mock = inject(MockApiService);
  return defer(() => of(mock.traiter(request))).pipe(delay(180));
};

@Injectable({ providedIn: 'root' })
export class MockApiService {
  private utilisateurs = structuredClone(utilisateursDemo);
  private sujets = structuredClone(sujetsDemo);
  private candidatures = structuredClone(candidaturesDemo);
  private prochainId = 100;

  traiter(request: HttpRequest<unknown>): HttpResponse<unknown> {
    const [ressource, identifiant, action] = request.url.slice(API_URL.length + 1).split('/');
    const id = Number(identifiant);
    const method = request.method;
    if (ressource === 'auth' && identifiant === 'connexion' && method === 'POST') {
      const data = request.body as { email: string; motDePasse: string };
      const compte = this.utilisateurs.find(u => u.email.toLowerCase() === data.email.trim().toLowerCase());
      if (!compte || data.motDePasse !== MOT_DE_PASSE_MOCK) return this.erreur(401, 'Adresse email ou mot de passe incorrect.');
      return this.reponse(compte);
    }
    if (ressource === 'utilisateurs-demo' && method === 'GET') return this.reponse(this.utilisateurs);
    const utilisateur = this.utilisateurs.find(u => u.id === Number(request.headers.get('X-Utilisateur-Id')));
    if (!utilisateur) return this.erreur(400, 'Veuillez sélectionner un utilisateur valide.');

    if (ressource === 'etudiants' || ressource === 'enseignants') {
      this.autoriser(utilisateur, ['ADMIN']);
      const role: Role = ressource === 'etudiants' ? 'ETUDIANT' : 'ENSEIGNANT';
      if (method === 'GET') return this.reponse(this.utilisateurs.filter(u => u.role === role));
      const personne = this.utilisateurs.find(u => u.id === id && u.role === role);
      if (method !== 'POST' && !personne) return this.erreur(404, 'Utilisateur introuvable.');
      if (method === 'DELETE') {
        if (this.sujets.some(s => s.enseignantId === id) || this.candidatures.some(c => c.etudiantId === id)) return this.erreur(409, 'Cet utilisateur possède des sujets ou des candidatures.');
        this.utilisateurs = this.utilisateurs.filter(u => u.id !== id);
        return this.reponse(null, 204);
      }
      if (method === 'POST' || method === 'PUT') {
        const data = request.body as PersonneForm;
        if (!data.nom?.trim() || !data.prenom?.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) return this.erreur(400, 'Renseignez un nom, un prénom et un email valide.');
        if (this.utilisateurs.some(u => u.email.toLowerCase() === data.email.toLowerCase() && u.id !== personne?.id)) return this.erreur(409, 'Cette adresse email est déjà utilisée.');
        const resultat: Utilisateur = { id: personne?.id ?? this.prochainId++, nom: data.nom, prenom: data.prenom, email: data.email, role };
        if (personne) Object.assign(personne, resultat); else this.utilisateurs.push(resultat);
        return this.reponse(resultat, personne ? 200 : 201);
      }
    }
    if (ressource === 'sujets') {
      if (method === 'GET') {
        let resultats = this.sujets.map(s => this.sujetComplet(s));
        if (utilisateur.role === 'ENSEIGNANT') resultats = resultats.filter(s => s.enseignantId === utilisateur.id);
        if (utilisateur.role === 'ETUDIANT') resultats = resultats.filter(s => s.disponible);
        return this.reponse(resultats);
      }
      this.autoriser(utilisateur, ['ENSEIGNANT']);
      const sujet = this.sujets.find(s => s.id === id);
      if (method !== 'POST' && !sujet) return this.erreur(404, 'Sujet introuvable.');
      if (sujet && sujet.enseignantId !== utilisateur.id) return this.erreur(403, 'Vous ne pouvez modifier que vos propres sujets.');
      if (method === 'DELETE') {
        if (this.candidatures.some(c => c.sujetId === id)) return this.erreur(409, 'Ce sujet possède des candidatures et ne peut pas être supprimé.');
        this.sujets = this.sujets.filter(s => s.id !== id);
        return this.reponse(null, 204);
      }
      if (method === 'POST' || method === 'PUT') {
        const data = request.body as SujetForm;
        if (!data.titre?.trim() || !data.description?.trim() || !Number.isInteger(data.capaciteMax) || data.capaciteMax < 1) return this.erreur(400, 'Vérifiez le titre, la description et la capacité.');
        const accepte = this.candidatures.filter(c => c.sujetId === id && c.statut === 'ACCEPTEE').length;
        if (data.capaciteMax < accepte) return this.erreur(409, 'La capacité ne peut pas être inférieure au nombre d’affectations.');
        const resultat: MockSujet = { id: sujet?.id ?? this.prochainId++, enseignantId: utilisateur.id, titre: data.titre, description: data.description, capaciteMax: data.capaciteMax, ouvert: data.ouvert };
        if (sujet) Object.assign(sujet, resultat); else this.sujets.push(resultat);
        this.candidatures.filter(c => c.sujetId === resultat.id).forEach(c => c.sujetTitre = resultat.titre);
        return this.reponse(this.sujetComplet(resultat), sujet ? 200 : 201);
      }
    }
    if (ressource === 'candidatures') {
      this.autoriser(utilisateur, ['ENSEIGNANT', 'ETUDIANT', 'RESPONSABLE']);
      if (method === 'GET') {
        let resultats = this.candidatures;
        if (utilisateur.role === 'ETUDIANT') resultats = resultats.filter(c => c.etudiantId === utilisateur.id);
        if (utilisateur.role === 'ENSEIGNANT') resultats = resultats.filter(c => this.sujets.some(s => s.id === c.sujetId && s.enseignantId === utilisateur.id));
        return this.reponse(resultats.map(c => ({ ...c, etudiantNom: this.nom(c.etudiantId) })));
      }
      if (method === 'POST') {
        this.autoriser(utilisateur, ['ETUDIANT']);
        const data = request.body as { sujetId: number; motivation: string };
        const sujet = this.sujets.find(s => s.id === data.sujetId);
        if (!sujet) return this.erreur(404, 'Sujet introuvable.');
        if (!data.motivation?.trim()) return this.erreur(400, 'La motivation est obligatoire.');
        if (this.candidatures.some(c => c.sujetId === sujet.id && c.etudiantId === utilisateur.id)) return this.erreur(409, 'Vous avez déjà candidaté à ce sujet.');
        if (!this.sujetComplet(sujet).disponible) return this.erreur(409, 'Ce sujet ne dispose plus de place.');
        const candidature: Candidature = { id: this.prochainId++, sujetId: sujet.id, sujetTitre: sujet.titre, etudiantId: utilisateur.id, etudiantNom: this.nom(utilisateur.id), motivation: data.motivation, dateCandidature: new Date().toISOString(), statut: 'EN_ATTENTE' };
        this.candidatures.push(candidature);
        return this.reponse(candidature, 201);
      }
      if (method === 'PATCH' && action === 'statut') {
        this.autoriser(utilisateur, ['RESPONSABLE']);
        const candidature = this.candidatures.find(c => c.id === id);
        if (!candidature) return this.erreur(404, 'Candidature introuvable.');
        if (candidature.statut !== 'EN_ATTENTE') return this.erreur(409, 'Une décision a déjà été prise.');
        const statut = (request.body as { statut: Statut }).statut;
        if (statut !== 'ACCEPTEE' && statut !== 'REJETEE') return this.erreur(400, 'Statut invalide.');
        const sujet = this.sujets.find(s => s.id === candidature.sujetId)!;
        if (statut === 'ACCEPTEE' && this.sujetComplet(sujet).placesRestantes < 1) return this.erreur(409, 'Il ne reste plus de place pour ce sujet.');
        candidature.statut = statut;
        return this.reponse(candidature);
      }
    }
    return this.erreur(404, 'Route de démonstration introuvable.');
  }
  private sujetComplet(sujet: MockSujet): Sujet {
    const placesRestantes = sujet.capaciteMax - this.candidatures.filter(c => c.sujetId === sujet.id && c.statut === 'ACCEPTEE').length;
    return { ...sujet, enseignantNom: this.nom(sujet.enseignantId), placesRestantes, disponible: sujet.ouvert && placesRestantes > 0 };
  }
  private nom(id: number): string { const u = this.utilisateurs.find(p => p.id === id); return u ? u.prenom + ' ' + u.nom : ''; }
  private autoriser(u: Utilisateur, roles: Role[]): void { if (!roles.includes(u.role)) this.erreur(403, 'Accès interdit pour ce profil.'); }
  private reponse(body: unknown, status = 200): HttpResponse<unknown> { return new HttpResponse({ body: structuredClone(body), status }); }
  private erreur(status: number, message: string): never { throw new HttpErrorResponse({ status, error: { message } }); }
}
