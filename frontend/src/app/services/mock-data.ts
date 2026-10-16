import { Candidature, SujetForm, Utilisateur } from '../models/models';

export const utilisateursDemo: Utilisateur[] = [
  { id: 1, nom: 'Ndiaye', prenom: 'Fatou', email: 'fatou.ndiaye@gdil.example', role: 'ADMIN' },
  { id: 2, nom: 'Diop', prenom: 'Moussa', email: 'moussa.diop@gdil.example', role: 'ENSEIGNANT' },
  { id: 3, nom: 'Fall', prenom: 'Awa', email: 'awa.fall@gdil.example', role: 'ENSEIGNANT' },
  { id: 4, nom: 'Diallo', prenom: 'Aminata', email: 'aminata.diallo@gdil.example', role: 'ETUDIANT' },
  { id: 5, nom: 'Sarr', prenom: 'Ibrahima', email: 'ibrahima.sarr@gdil.example', role: 'ETUDIANT' },
  { id: 6, nom: 'Ba', prenom: 'Mariama', email: 'mariama.ba@gdil.example', role: 'ETUDIANT' },
  { id: 7, nom: 'Seck', prenom: 'Cheikh', email: 'cheikh.seck@gdil.example', role: 'RESPONSABLE' }
];
export interface MockSujet extends SujetForm { id: number; enseignantId: number; }
export const sujetsDemo: MockSujet[] = [
  { id: 1, enseignantId: 2, titre: 'Suivi des stages à Dakar', description: 'Concevoir une application web permettant aux étudiants de suivre leurs demandes de stage auprès des entreprises de Dakar et aux enseignants de centraliser les conventions et les évaluations. Le projet inclut une API REST et une interface de suivi.', capaciteMax: 3, ouvert: true },
  { id: 2, enseignantId: 3, titre: 'Réservation de salles universitaires', description: 'Développer un outil de réservation des salles de travail du campus de Saint-Louis : consultation des disponibilités, gestion des créneaux et prévention des conflits de réservation.', capaciteMax: 2, ouvert: true },
  { id: 3, enseignantId: 2, titre: 'Bibliothèque numérique du campus', description: 'Réaliser un catalogue de ressources pédagogiques pour les étudiants au Sénégal avec recherche, classement par module et gestion des emprunts. Une attention particulière sera portée à l’accessibilité.', capaciteMax: 2, ouvert: true },
  { id: 4, enseignantId: 3, titre: 'Suivi énergétique du campus de Thiès', description: 'Créer une interface de consultation des relevés énergétiques des bâtiments universitaires à Thiès et de comparaison des consommations par période.', capaciteMax: 2, ouvert: false }
];
export const candidaturesDemo: Candidature[] = [
  { id: 1, etudiantId: 4, etudiantNom: 'Aminata Diallo', sujetId: 1, sujetTitre: 'Suivi des stages à Dakar', motivation: 'Je souhaite approfondir mes compétences en Angular et Spring Boot en travaillant sur un outil utile aux étudiants.', dateCandidature: '2026-10-12T09:30:00Z', statut: 'EN_ATTENTE' },
  { id: 2, etudiantId: 5, etudiantNom: 'Ibrahima Sarr', sujetId: 2, sujetTitre: 'Réservation de salles universitaires', motivation: 'La gestion des disponibilités et des conflits m’intéresse particulièrement. Je souhaite participer à la conception de cette application.', dateCandidature: '2026-10-11T14:15:00Z', statut: 'ACCEPTEE' },
  { id: 3, etudiantId: 4, etudiantNom: 'Aminata Diallo', sujetId: 3, sujetTitre: 'Bibliothèque numérique du campus', motivation: 'Je voudrais contribuer à rendre les ressources pédagogiques plus accessibles.', dateCandidature: '2026-10-10T11:00:00Z', statut: 'REJETEE' },
  { id: 4, etudiantId: 6, etudiantNom: 'Mariama Ba', sujetId: 1, sujetTitre: 'Suivi des stages à Dakar', motivation: 'Je souhaite travailler sur la modélisation des données et la conception des formulaires de suivi.', dateCandidature: '2026-10-13T08:45:00Z', statut: 'EN_ATTENTE' }
];

export const MOT_DE_PASSE_MOCK = 'Projet2026!';
