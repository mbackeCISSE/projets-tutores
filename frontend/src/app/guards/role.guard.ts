import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';
import { Role } from '../models/models';
export const roleGuard: CanActivateFn = route => {
  const utilisateur = inject(SessionService).utilisateur();
  const router = inject(Router);
  if (!utilisateur) return router.createUrlTree(['/connexion']);
  const roles = route.data['roles'] as Role[];
  return roles.includes(utilisateur.role) || router.createUrlTree(['/sujets']);
};
