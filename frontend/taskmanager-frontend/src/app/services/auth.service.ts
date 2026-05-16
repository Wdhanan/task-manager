// src/app/services/auth.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth-response';
import { User } from '../models/user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Beim App-Start prüfen ob noch ein gültiges Token vorhanden ist
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          this.saveUser(response);
        })
      );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          this.saveUser(response);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      // JWT Token hat 3 Teile: Header.Payload.Signatur
      // Der mittlere Teil (Payload) ist Base64-kodiert
      const payload = JSON.parse(atob(token.split('.')[1]));
      const isExpired = payload.exp * 1000 < Date.now();

      if (isExpired) {
        // ✅ Token abgelaufen → sofort aufräumen!
        // Verhindert dass Angular immer wieder ein ungültiges Token mitschickt
        console.log('Token abgelaufen – automatisch ausloggen');
        this.clearStorage();
        return false;
      }

      return true;
    } catch {
      // Token hat ungültiges Format → auch löschen
      this.clearStorage();
      return false;
    }
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  private saveUser(response: AuthResponse): void {
    const user: User = {
      id: response.id,
      username: response.username,
      email: response.email,
      firstName: response.username,
      lastName: '',
      role: response.role as 'USER' | 'ADMIN',
      createdAt: new Date().toISOString()
    };
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private clearStorage(): void {
    // ✅ Alles löschen ohne navigate (vermeidet Redirect-Schleifen)
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem('user');
    if (userStr && this.isLoggedIn()) {
      // isLoggedIn() prüft automatisch ob Token noch gültig ist
      this.currentUserSubject.next(JSON.parse(userStr));
    }
    // Falls Token abgelaufen: isLoggedIn() hat bereits clearStorage() aufgerufen
  }
}