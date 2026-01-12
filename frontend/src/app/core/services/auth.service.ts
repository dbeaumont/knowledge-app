import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  username: string | null = null;
  roles: string[] = [];
  private profileLoaded = false;

  constructor(private http: HttpClient) {
  }

  loadProfile() {
    return this.http.get<{ username: string; roles: Array<{ authority: string }> | string[] }>(`/api/users/me`).pipe(
      tap(res => {
        this.username = res.username;
        this.roles = this.normalizeRoles(res.roles);
        this.profileLoaded = true;
      }),
      catchError(() => {
        this.username = null;
        this.roles = [];
        this.profileLoaded = true;
        return of(null);
      })
    );
  }

  login() {
    window.location.assign('/oauth2/authorization/keycloak');
  }

  logout() {
    window.location.assign('/logout');
  }

  isAuthenticated() {
    return !!this.username;
  }

  isProfileReady() {
    return this.profileLoaded;
  }

  private normalizeRoles(roles: Array<{ authority: string }> | string[] | null | undefined): string[] {
    if (!roles) {
      return [];
    }
    if (Array.isArray(roles) && roles.length > 0 && typeof roles[0] === 'string') {
      return roles as string[];
    }
    if (Array.isArray(roles)) {
      return (roles as Array<{ authority: string }>).map(role => role.authority);
    }
    return [];
  }
}
