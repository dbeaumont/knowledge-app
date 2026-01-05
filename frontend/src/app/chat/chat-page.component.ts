import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../core/services/chat.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="panel">
      <h2>Chat</h2>
      <div class="history" *ngIf="messages.length; else empty">
        <div *ngFor="let message of messages" class="bubble" [class.me]="message.role==='user'">
          <strong>{{ message.role }}:</strong>
          <div>{{ message.content }}</div>
        </div>
        <div *ngIf="loading" class="bubble loading">
          <div class="spinner"></div>
          <span>Le modèle réfléchit...</span>
        </div>
      </div>
      <ng-template #empty>
        <p class="muted">Posez une question pour démarrer.</p>
      </ng-template>
      <form (ngSubmit)="send()" class="composer">
        <textarea [(ngModel)]="draft" name="draft" required rows="3" placeholder="Demandez quelque chose..." [disabled]="loading"></textarea>
        <button type="submit" [disabled]="loading">Envoyer</button>
      </form>
      <p class="muted" *ngIf="error">{{ error }}</p>
    </section>
  `,
  styles: [`
    .panel { max-width: 960px; margin: 0 auto; display: grid; gap: 12px; }
    .history { background: #111827; border-radius: 8px; padding: 12px; min-height: 240px; }
    .bubble { padding: 8px 10px; margin-bottom: 8px; border-radius: 6px; background: #0f172a; }
    .bubble.me { background: #1e293b; }
    .bubble.loading { display: flex; align-items: center; gap: 8px; font-style: italic; color: #cbd5e1; }
    .spinner { width: 14px; height: 14px; border: 2px solid #94a3b8; border-top-color: transparent; border-radius: 50%; animation: spin 0.8s linear infinite; }
    .composer { display: flex; flex-direction: column; gap: 8px; }
    textarea { background: #0f172a; color: #e2e8f0; border: 1px solid #1f2937; border-radius: 6px; padding: 10px; }
    button { align-self: flex-end; padding: 8px 14px; border: none; border-radius: 6px; background: #22c55e; color: #0b1220; font-weight: 600; cursor: pointer; }
    button[disabled], textarea[disabled] { opacity: 0.6; cursor: not-allowed; }
    .muted { color: #94a3b8; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class ChatPageComponent {
  messages: ChatMessage[] = [];
  draft = '';
  loading = false;
  error = '';

  constructor(private chatService: ChatService) {}

  send() {
    if (!this.draft.trim()) return;
    if (this.loading) return;
    const prompt = this.draft.trim();
    this.messages.push({ role: 'user', content: prompt });
    this.draft = '';
    this.error = '';
    this.loading = true;
    this.chatService.ask(prompt).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: answer => this.messages.push({ role: 'assistant', content: answer }),
      error: err => this.error = 'Réponse indisponible (gateway ou LLM). Réessayez.'
    });
  }
}
