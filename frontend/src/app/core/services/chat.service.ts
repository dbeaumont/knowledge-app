import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

export interface ChatMessage { role: 'user' | 'assistant'; content: string; }

@Injectable({ providedIn: 'root' })
export class ChatService {
  constructor(private http: HttpClient) {}

  ask(query: string): Observable<string> {
    return this.http.post<{ answer: string }>(`/api/rag/answer`, { query }).pipe(map(res => res.answer));
  }
}
