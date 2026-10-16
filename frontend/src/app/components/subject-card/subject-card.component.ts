import { Component, input, output } from '@angular/core';
import { Role, Sujet } from '../../models/models';
import { IconComponent } from '../icon/icon.component';

@Component({ selector: 'app-subject-card', imports: [IconComponent], templateUrl: './subject-card.component.html' })
export class SubjectCardComponent {
  readonly sujet = input.required<Sujet>();
  readonly role = input.required<Role>();
  readonly busy = input(false);
  readonly dejaCandidate = input(false);
  readonly modifier = output<Sujet>();
  readonly supprimer = output<Sujet>();
  readonly candidater = output<Sujet>();
}
