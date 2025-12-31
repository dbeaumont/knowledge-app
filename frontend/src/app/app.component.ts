import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <main class="shell">
      <header class="topbar">
        <h1>AI Knowledge Workspace</h1>
        <nav>
          <a routerLink="/chat" routerLinkActive="active">Chat</a>
          <a routerLink="/documents" routerLinkActive="active">Documents</a>
          <a routerLink="/profile" routerLinkActive="active">Profile</a>
        </nav>
      </header>
      <section class="content">
        <router-outlet></router-outlet>
      </section>
    </main>
  `,
  styles: [`
    .shell { font-family: 'Inter', system-ui, -apple-system, sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
    .topbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 18px; background: linear-gradient(120deg, #0f172a, #111827); color: #f8fafc; }
    nav a { color: #cbd5e1; margin-left: 12px; text-decoration: none; font-weight: 500; }
    nav a.active { color: #fbbf24; }
    .content { padding: 16px; flex: 1; background: #0b1220; color: #e2e8f0; }
  `]
})
export class AppComponent {}
