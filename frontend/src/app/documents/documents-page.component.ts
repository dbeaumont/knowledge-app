import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentsService, DocumentItem } from '../core/services/documents.service';

@Component({
  selector: 'app-documents-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="panel">
      <h2>Documents</h2>
      <form (ngSubmit)="save()" class="composer">
        <input [(ngModel)]="name" name="name" placeholder="Nom" required />
        <textarea [(ngModel)]="description" name="description" placeholder="Description"></textarea>
        <button type="submit">Ajouter</button>
      </form>
      <div class="list">
        <article *ngFor="let doc of documents" class="card">
          <h3>{{ doc.name }}</h3>
          <p>{{ doc.description }}</p>
          <small>Status: {{ doc.status }} — {{ doc.createdAt | date:'short' }}</small>
        </article>
      </div>
    </section>
  `,
  styles: [`
    .panel { max-width: 960px; margin: 0 auto; display: grid; gap: 12px; }
    .composer { display: grid; gap: 8px; grid-template-columns: 1fr; }
    input, textarea { background: #0f172a; color: #e2e8f0; border: 1px solid #1f2937; border-radius: 6px; padding: 10px; }
    button { justify-self: flex-start; padding: 8px 14px; border: none; border-radius: 6px; background: #38bdf8; color: #0b1220; font-weight: 600; cursor: pointer; }
    .list { display: grid; gap: 10px; }
    .card { padding: 10px; border-radius: 8px; background: #0f172a; }
  `]
})
export class DocumentsPageComponent implements OnInit {
  documents: DocumentItem[] = [];
  name = '';
  description = '';

  constructor(private documentsService: DocumentsService) {}

  ngOnInit(): void {
    this.refresh();
  }

  save() {
    this.documentsService.create({ name: this.name, description: this.description }).subscribe(doc => {
      this.documents.unshift(doc);
      this.name = '';
      this.description = '';
    });
  }

  refresh() {
    this.documentsService.list().subscribe(docs => this.documents = docs);
  }
}
