import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="panel">
      <h2>Profil</h2>
      <div *ngIf="auth.isAuthenticated(); else loginBlock" class="card">
        <p><strong>Utilisateur:</strong> {{ auth.username }}</p>
        <button (click)="auth.logout()">Déconnexion</button>
      </div>
      <ng-template #loginBlock>
        <form (ngSubmit)="login()" class="composer">
          <input [(ngModel)]="username" name="username" placeholder="Utilisateur" required />
          <input [(ngModel)]="password" name="password" type="password" placeholder="Mot de passe" required />
          <button type="submit">Connexion</button>
        </form>
      </ng-template>
    </section>
  `,
  styles: [`
    .panel { max-width: 640px; margin: 0 auto; display: grid; gap: 12px; }
    .composer { display: grid; gap: 8px; }
    input { background: #0f172a; color: #e2e8f0; border: 1px solid #1f2937; border-radius: 6px; padding: 10px; }
    button { justify-self: flex-start; padding: 8px 14px; border: none; border-radius: 6px; background: #a78bfa; color: #0b1220; font-weight: 600; cursor: pointer; }
    .card { padding: 10px; border-radius: 8px; background: #0f172a; }
  `]
})
export class ProfilePageComponent {
  username = '';
  password = '';

  constructor(public auth: AuthService) {}

  login() {
    this.auth.login(this.username, this.password).subscribe();
  }
}
