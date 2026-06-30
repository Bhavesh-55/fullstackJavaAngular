import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { LoginRequest } from '../models/auth/login-request.model';
import { SignupRequest } from '../models/auth/signup-request.model';
import { AuthResponse } from '../models/auth/auth-response.model';
import { RefreshTokenRequest } from '../models/auth/refresh-token-request.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiBaseUrl}/auth`;

  private readonly accessTokenKey = 'accessToken';
  private readonly refreshTokenKey = 'refreshToken';
  private readonly userKey = 'loggedInUser';

  currentUser = signal<AuthResponse | null>(this.getUserFromStorage());

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, request)
      .pipe(
        tap(response => this.saveAuthData(response))
      );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => this.saveAuthData(response))
      );
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    const request: RefreshTokenRequest = {
      refreshToken: refreshToken || ''
    };

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, request)
      .pipe(
        tap(response => this.saveAuthData(response))
      );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();

    if (refreshToken) {
      this.http.post<void>(`${this.apiUrl}/logout`, { refreshToken })
        .subscribe({
          next: () => this.clearAuthData(),
          error: () => this.clearAuthData()
        });

      return;
    }

    this.clearAuthData();
  }

  clearAuthData(): void {
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.userKey);

    this.currentUser.set(null);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }

  private saveAuthData(response: AuthResponse): void {
    localStorage.setItem(this.accessTokenKey, response.accessToken);
    localStorage.setItem(this.refreshTokenKey, response.refreshToken);
    localStorage.setItem(this.userKey, JSON.stringify(response));

    this.currentUser.set(response);
  }

  private getUserFromStorage(): AuthResponse | null {
    const userJson = localStorage.getItem(this.userKey);

    if (!userJson) {
      return null;
    }

    try {
      return JSON.parse(userJson) as AuthResponse;
    } catch {
      this.clearAuthData();
      return null;
    }
  }
}