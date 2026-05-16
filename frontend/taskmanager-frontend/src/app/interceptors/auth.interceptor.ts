// src/app/interceptors/auth.interceptor.ts

import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent,
  HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    const token = this.authService.getToken();

    // Token vorhanden → zu jedem Request hinzufügen
    if (token) {
      request = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {

        if (error.status === 401) {
          // 401 = Nicht authentifiziert → Token abgelaufen oder ungültig
          // Automatisch ausloggen und zur Login-Seite
          console.log('401 empfangen → automatisch ausloggen');
          this.authService.logout();
        }

        if (error.status === 403) {
          // 403 = Verboten → Keine Berechtigung für diese Aktion
          // (Nicht dasselbe wie 401!)
          // Nur ausloggen wenn wir wirklich keinen Token haben
          if (!this.authService.isLoggedIn()) {
            this.authService.logout();
          }
        }

        return throwError(() => error);
      })
    );
  }
}