import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Candidature, PersonneForm, Statut, Sujet, SujetForm, Utilisateur } from '../models/models';
import { SessionService } from './session.service';
export const API_URL = 'http://localhost:8080/api';
export const utilisateurInterceptor: HttpInterceptorFn = (request, next) => {
  const utilisateur = inject(SessionService).utilisateur();
  if (utilisateur && request.url.startsWith(API_URL + '/') && request.url !== API_URL + '/utilisateurs-demo' && request.url !== API_URL + '/auth/connexion') {
    request = request.clone({ setHeaders: { 'X-Utilisateur-Id': String(utilisateur.id) } });
  }
  return next(request);
};
export function messageErreur(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'Le serveur est inaccessible. Vérifiez que le backend est démarré sur http://localhost:8080 et autorise le frontend.';
    const body: unknown = error.error;
    if (typeof body === 'object' && body !== null && 'message' in body && typeof body.message === 'string') return body.message;
    return 'La requête a échoué (erreur ' + error.status + '). Veuillez réessayer.';
  }
  return 'Une erreur est survenue. Veuillez réessayer.';
}
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  connexion(email: string, motDePasse: string) { return this.http.post<Utilisateur>(API_URL + '/auth/connexion', { email, motDePasse }); }
  utilisateurs() { return this.http.get<Utilisateur[]>(API_URL + '/utilisateurs-demo'); }
  personnes(type: 'etudiants' | 'enseignants') { return this.http.get<Utilisateur[]>(API_URL + '/' + type); }
  enregistrerPersonne(type: 'etudiants' | 'enseignants', data: PersonneForm, id: number | null) {
    return id === null ? this.http.post<Utilisateur>(API_URL + '/' + type, data) : this.http.put<Utilisateur>(API_URL + '/' + type + '/' + id, data);
  }
  supprimerPersonne(type: 'etudiants' | 'enseignants', id: number) { return this.http.delete<void>(API_URL + '/' + type + '/' + id); }
  sujets() { return this.http.get<Sujet[]>(API_URL + '/sujets'); }
  enregistrerSujet(data: SujetForm, id: number | null) {
    return id === null ? this.http.post<Sujet>(API_URL + '/sujets', data) : this.http.put<Sujet>(API_URL + '/sujets/' + id, data);
  }
  supprimerSujet(id: number) { return this.http.delete<void>(API_URL + '/sujets/' + id); }
  candidatures() { return this.http.get<Candidature[]>(API_URL + '/candidatures'); }
  candidater(sujetId: number, motivation: string) { return this.http.post<Candidature>(API_URL + '/candidatures', { sujetId, motivation }); }
  decider(id: number, statut: Exclude<Statut, 'EN_ATTENTE'>) { return this.http.patch<Candidature>(API_URL + '/candidatures/' + id + '/statut', { statut }); }
}
