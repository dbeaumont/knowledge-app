import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../core/services/chat.service';

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
      </div>
      <ng-template #empty>
        <p class="muted">Posez une question pour démarrer.</p>
      </ng-template>
      <form (ngSubmit)="send()" class="composer">
        <textarea [(ngModel)]="draft" name="draft" required rows="3" placeholder="Demandez quelque chose..." ></textarea>
        <button type="submit">Envoyer</button>
      </form>
    </section>
  `,
  styles: [`
    .panel { max-width: 960px; margin: 0 auto; display: grid; gap: 12px; }
    .history { background: #111827; border-radius: 8px; padding: 12px; min-height: 240px; }
    .bubble { padding: 8px 10px; margin-bottom: 8px; border-radius: 6px; background: #0f172a; }
    .bubble.me { background: #1e293b; }
    .composer { display: flex; flex-direction: column; gap: 8px; }
    textarea { background: #0f172a; color: #e2e8f0; border: 1px solid #1f2937; border-radius: 6px; padding: 10px; }
    button { align-self: flex-end; padding: 8px 14px; border: none; border-radius: 6px; background: #22c55e; color: #0b1220; font-weight: 600; cursor: pointer; }
    .muted { color: #94a3b8; }
  `]
})
export class ChatPageComponent {
  messages: ChatMessage[] = [];
  draft = '';

  constructor(private chatService: ChatService) {}

  send() {
    if (!this.draft.trim()) return;
    const prompt = this.draft.trim();
    this.messages.push({ role: 'user', content: prompt });
    this.draft = '';
    this.chatService.ask(prompt).subscribe(answer => {
      this.messages.push({ role: 'assistant', content: answer });
    });
  }
}
