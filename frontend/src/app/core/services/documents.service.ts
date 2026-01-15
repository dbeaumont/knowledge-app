import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DocumentItem {
  id: string;
  name: string;
  description: string;
  originalFilename?: string;
  status: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  constructor(private http: HttpClient) {}

  list(): Observable<DocumentItem[]> {
    return this.http.get<DocumentItem[]>(`/api/documents`);
  }

  create(payload: { name?: string; description?: string; content?: string; file?: File }): Observable<DocumentItem> {
    const formData = new FormData();
    if (payload.file) {
      formData.append('file', payload.file, payload.file.name);
    }
    if (payload.name) {
      formData.append('name', payload.name);
    }
    if (payload.description) {
      formData.append('description', payload.description);
    }
    if (payload.content) {
      formData.append('content', payload.content);
    }
    return this.http.post<DocumentItem>(`/api/documents`, formData);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/documents/${id}`);
  }

  update(id: string, payload: { name: string; description?: string }): Observable<DocumentItem> {
    return this.http.put<DocumentItem>(`/api/documents/${id}`, payload);
  }
}
