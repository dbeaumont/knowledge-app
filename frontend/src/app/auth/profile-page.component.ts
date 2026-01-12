import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="panel">
      <h2>Profil</h2>
      <div *ngIf="auth.isAuthenticated(); else loginBlock" class="card">
        <p><strong>Utilisateur:</strong> {{ auth.username }}</p>
        <button (click)="auth.logout()">Déconnexion</button>
      </div>
      <ng-template #loginBlock>
        <p>Connecte-toi via le fournisseur OIDC.</p>
        <button (click)="auth.login()">Connexion</button>
      </ng-template>
    </section>
  `,
  styles: [`
    .panel { max-width: 640px; margin: 0 auto; display: grid; gap: 12px; }
    button { justify-self: flex-start; padding: 8px 14px; border: none; border-radius: 6px; background: #a78bfa; color: #0b1220; font-weight: 600; cursor: pointer; }
    .card { padding: 10px; border-radius: 8px; background: #0f172a; }
  `]
})
export class ProfilePageComponent {
  constructor(public auth: AuthService) {
    this.auth.loadProfile().subscribe();
  }
}
