import { Routes } from '@angular/router';
import { roleGuard } from './guards/role.guard';
import { ConnexionComponent } from './pages/connexion.component';
import { PersonnesComponent } from './pages/personnes.component';
import { SujetsComponent } from './pages/sujets.component';
import { CandidaturesComponent } from './pages/candidatures.component';
export const routes: Routes = [
  { path: 'connexion', component: ConnexionComponent },
  { path: 'selection', redirectTo: 'connexion', pathMatch: 'full' },
  { path: 'etudiants', component: PersonnesComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'], type: 'etudiants' } },
  { path: 'enseignants', component: PersonnesComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'], type: 'enseignants' } },
  { path: 'sujets', component: SujetsComponent, canActivate: [roleGuard], data: { roles: ['ADMIN', 'ENSEIGNANT', 'ETUDIANT', 'RESPONSABLE'] } },
  { path: 'candidatures', component: CandidaturesComponent, canActivate: [roleGuard], data: { roles: ['ENSEIGNANT', 'ETUDIANT', 'RESPONSABLE'] } },
  { path: 'affectations', component: CandidaturesComponent, canActivate: [roleGuard], data: { roles: ['RESPONSABLE'], affectations: true } },
  { path: '', pathMatch: 'full', redirectTo: 'connexion' },
  { path: '**', redirectTo: 'connexion' }
];
