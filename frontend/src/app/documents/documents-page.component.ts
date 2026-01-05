import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentsService, DocumentItem } from '../core/services/documents.service';
import { finalize } from 'rxjs/operators';

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

        <div class="dropzone"
             [class.active]="dragOver"
             (dragover)="onDragOver($event)"
             (dragleave)="onDragLeave()"
             (drop)="onDrop($event)">
          <p *ngIf="!fileName">Glissez un fichier texte ici ou cliquez pour sélectionner</p>
          <p *ngIf="fileName">Fichier chargé : <strong>{{ fileName }}</strong></p>
          <button type="button" (click)="fileInput.click()">Choisir un fichier</button>
          <input #fileInput type="file" accept=".txt,text/plain" hidden (change)="onFileSelected($event)" />
        </div>

        <textarea [(ngModel)]="content" name="content" placeholder="Ou collez du texte à indexer"></textarea>
        <button type="submit" [disabled]="isSaving">
          {{ isSaving ? 'Ajout...' : 'Ajouter' }}
        </button>
        <div *ngIf="isSaving" class="loading-row">
          <div class="spinner"></div>
          <span>Ajout et indexation en cours...</span>
        </div>
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
    button[disabled] { opacity: 0.6; cursor: not-allowed; }
    .dropzone { border: 2px dashed #334155; border-radius: 8px; padding: 16px; text-align: center; background: #0b1220; color: #cbd5e1; }
    .dropzone.active { border-color: #38bdf8; color: #38bdf8; }
    .list { display: grid; gap: 10px; }
    .card { padding: 10px; border-radius: 8px; background: #0f172a; }
    .loading-row { display: inline-flex; align-items: center; gap: 8px; color: #94a3b8; font-size: 0.95rem; }
    .spinner { width: 14px; height: 14px; border: 2px solid #94a3b8; border-top-color: transparent; border-radius: 50%; animation: spin 0.8s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class DocumentsPageComponent implements OnInit {
  documents: DocumentItem[] = [];
  name = '';
  description = '';
  content = '';
  fileName = '';
  dragOver = false;
   isSaving = false;
   error = '';

  constructor(private documentsService: DocumentsService) {}

  ngOnInit(): void {
    this.refresh();
  }

  save() {
    if (this.isSaving) {
      return;
    }
    this.isSaving = true;
    this.error = '';
    this.documentsService.create({ name: this.name, description: this.description, content: this.content })
      .pipe(finalize(() => this.isSaving = false))
      .subscribe({
        next: doc => {
          this.name = '';
          this.description = '';
          this.content = '';
          this.fileName = '';
          this.refresh();
        },
        error: () => {
          this.error = 'Échec de l’ajout (auth ou réseau). Réessaie.';
        }
      });
  }

  refresh() {
    this.documentsService.list().subscribe(docs => this.documents = docs);
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.dragOver = true;
  }

  onDragLeave() {
    this.dragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) {
      this.readFile(file);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.readFile(file);
    }
  }

  private readFile(file: File) {
    this.fileName = file.name;
    const reader = new FileReader();
    reader.onload = () => {
      this.content = (reader.result as string) || '';
    };
    reader.readAsText(file);
  }
}
