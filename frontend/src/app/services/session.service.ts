import { Injectable, signal } from '@angular/core';
import { Utilisateur } from '../models/models';
@Injectable({ providedIn: 'root' })
export class SessionService { readonly utilisateur = signal<Utilisateur | null>(null); }