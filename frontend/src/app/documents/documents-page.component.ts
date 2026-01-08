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
        <p class="helper">Formats acceptes : TXT, PDF, EPUB.</p>

        <div class="dropzone"
             [class.active]="dragOver"
             (dragover)="onDragOver($event)"
             (dragleave)="onDragLeave()"
             (drop)="onDrop($event)">
          <p *ngIf="!fileName">Glissez un fichier texte, PDF ou EPUB ici ou cliquez pour sélectionner</p>
          <p *ngIf="fileName">Fichier chargé : <strong>{{ fileName }}</strong></p>
          <button type="button" (click)="fileInput.click()">Choisir un fichier</button>
          <input #fileInput type="file" accept=".txt,.pdf,.epub,text/plain,application/pdf,application/epub+zip" hidden (change)="onFileSelected($event)" />
        </div>

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
          <div class="card-header">
            <h3>{{ doc.name }}</h3>
            <div class="card-actions">
              <button type="button" class="icon-button edit" (click)="openEdit(doc)" aria-label="Modifier" title="Modifier">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 20h4l10-10a2 2 0 0 0-4-4L4 16v4z"/>
                  <path d="M13 7l4 4"/>
                </svg>
              </button>
              <button type="button" class="icon-button delete" (click)="remove(doc)" aria-label="Supprimer" title="Supprimer">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M6 6l12 12"/>
                  <path d="M18 6l-12 12"/>
                </svg>
              </button>
            </div>
          </div>
          <p *ngIf="doc.originalFilename" class="filename">Fichier: {{ doc.originalFilename }}</p>
          <p>{{ doc.description }}</p>
          <small>Status: {{ doc.status }} — {{ doc.createdAt | date:'short' }}</small>
        </article>
      </div>

      <div *ngIf="editingDoc" class="modal-backdrop" (click)="closeEdit()"></div>
      <div *ngIf="editingDoc" class="modal" (click)="$event.stopPropagation()">
        <h3>Modifier le document</h3>
        <input [(ngModel)]="editName" name="editName" placeholder="Nom" required />
        <textarea [(ngModel)]="editDescription" name="editDescription" placeholder="Description"></textarea>
        <div class="modal-actions">
          <button type="button" class="ghost" (click)="closeEdit()">Annuler</button>
          <button type="button" (click)="saveEdit()" [disabled]="isSaving">Enregistrer</button>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .panel { max-width: 960px; margin: 0 auto; display: grid; gap: 12px; }
    .composer { display: grid; gap: 8px; grid-template-columns: 1fr; }
    input, textarea { background: #0f172a; color: #e2e8f0; border: 1px solid #1f2937; border-radius: 6px; padding: 10px; }
    .helper { margin: 0; font-size: 0.9rem; color: #94a3b8; }
    button { justify-self: flex-start; padding: 8px 14px; border: none; border-radius: 6px; background: #38bdf8; color: #0b1220; font-weight: 600; cursor: pointer; }
    button[disabled] { opacity: 0.6; cursor: not-allowed; }
    .dropzone { border: 2px dashed #334155; border-radius: 8px; padding: 16px; text-align: center; background: #0b1220; color: #cbd5e1; }
    .dropzone.active { border-color: #38bdf8; color: #38bdf8; }
    .list { display: grid; gap: 10px; }
    .card { padding: 10px; border-radius: 8px; background: #0f172a; }
    .card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
    .card-actions { display: inline-flex; align-items: center; gap: 6px; }
    .filename { margin: 0; color: #cbd5f5; font-size: 0.9rem; }
    .icon-button { display: inline-flex; align-items: center; justify-content: center; width: 56px; height: 56px; border-radius: 12px; border: none; background: transparent; color: #ef4444; }
    .icon-button svg { width: 30px; height: 30px; stroke: currentColor; fill: none; stroke-width: 2.2; stroke-linecap: round; stroke-linejoin: round; }
    .icon-button.edit { color: #38bdf8; }
    .icon-button.delete { color: #ef4444; }
    .modal-backdrop { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); }
    .modal { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #0b1220; border: 1px solid #1f2937; border-radius: 12px; padding: 16px; display: grid; gap: 10px; width: min(520px, 92vw); }
    .modal h3 { margin: 0; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 8px; }
    .ghost { background: transparent; border: 1px solid #475569; color: #e2e8f0; }
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
  selectedFile: File | null = null;
  dragOver = false;
   isSaving = false;
   error = '';
  editingDoc: DocumentItem | null = null;
  editName = '';
  editDescription = '';

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
    const request$ = this.documentsService.create({
      name: this.name,
      description: this.description,
      content: this.selectedFile ? undefined : this.content,
      file: this.selectedFile || undefined
    });
    request$
      .pipe(finalize(() => this.isSaving = false))
      .subscribe({
        next: doc => {
          this.name = '';
          this.description = '';
          this.content = '';
          this.fileName = '';
          this.selectedFile = null;
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

  openEdit(doc: DocumentItem) {
    this.editingDoc = doc;
    this.editName = doc.name;
    this.editDescription = doc.description || '';
  }

  closeEdit() {
    this.editingDoc = null;
    this.editName = '';
    this.editDescription = '';
  }

  saveEdit() {
    if (!this.editingDoc || this.isSaving) {
      return;
    }
    this.isSaving = true;
    this.documentsService.update(this.editingDoc.id, {
      name: this.editName,
      description: this.editDescription
    }).pipe(finalize(() => this.isSaving = false))
      .subscribe({
        next: () => {
          this.closeEdit();
          this.refresh();
        },
        error: () => {
          this.error = 'Échec de la modification (auth ou réseau). Réessaie.';
        }
      });
  }

  remove(doc: DocumentItem) {
    if (!confirm(`Supprimer "${doc.name}" ?`)) {
      return;
    }
    this.documentsService.delete(doc.id).subscribe({
      next: () => this.refresh(),
      error: () => {
        this.error = 'Échec de la suppression (auth ou réseau). Réessaie.';
      }
    });
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
      this.handleFile(file);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.handleFile(file);
    }
  }

  private handleFile(file: File) {
    this.fileName = file.name;
    this.selectedFile = file;
    this.content = '';
  }
}
