export type Role = 'ADMIN' | 'ENSEIGNANT' | 'ETUDIANT' | 'RESPONSABLE';
export type Statut = 'EN_ATTENTE' | 'ACCEPTEE' | 'REJETEE';
export interface Utilisateur { id: number; nom: string; prenom: string; email: string; role: Role; }
export interface PersonneForm { nom: string; prenom: string; email: string; }
export interface SujetForm { titre: string; description: string; capaciteMax: number; ouvert: boolean; }
export interface Sujet extends SujetForm { id: number; enseignantId: number; enseignantNom: string; placesRestantes: number; disponible: boolean; }
export interface Candidature { id: number; etudiantId: number; etudiantNom: string; sujetId: number; sujetTitre: string; motivation: string; dateCandidature: string; statut: Statut; }
export const roles: Record<Role, string> = { ADMIN: 'Administrateur', ENSEIGNANT: 'Enseignant', ETUDIANT: 'Étudiant', RESPONSABLE: 'Responsable' };
export const statuts: Record<Statut, string> = { EN_ATTENTE: 'En attente', ACCEPTEE: 'Acceptée', REJETEE: 'Rejetée' };