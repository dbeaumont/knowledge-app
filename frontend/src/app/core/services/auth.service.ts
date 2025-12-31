import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'ai-workspace-token';
  username: string | null = null;

  constructor(private http: HttpClient) {
    this.username = localStorage.getItem('username');
  }

  login(username: string, password: string) {
    return this.http.post<{ token: string; username: string }>(`/api/auth/login`, { username, password }).pipe(
      tap(res => {
        localStorage.setItem(this.tokenKey, res.token);
        localStorage.setItem('username', res.username);
        this.username = res.username;
      })
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('username');
    this.username = null;
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated() {
    return !!this.token;
  }
}
