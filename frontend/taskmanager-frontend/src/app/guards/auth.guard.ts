import { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

// CanActivateFn = Eine Funktion die entscheidet: "Darf der User diese Seite sehen?"
export const authGuard = (): boolean => {
  const authService = inject(AuthService); // ← inject() = Service holen
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;  // ✅ Eingeloggt → Seite zeigen
  } else {
    router.navigate(['/login']); // ❌ Nicht eingeloggt → zur Login-Seite
    return false;
  }
};