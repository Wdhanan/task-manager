// src/app/pages/login/login.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
// FormBuilder  = Hilft uns Formulare zu bauen
// FormGroup    = Eine Gruppe von Formularfeldern
// Validators   = Regeln: "Dieses Feld ist Pflicht", "Min. 8 Zeichen", etc.

import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;  // Das "!" sagt TypeScript: "Wird sicher initialisiert"
  isLoading = false;      // Zeigt Lade-Spinner
  errorMessage = '';      // Fehlermeldung für den User

  constructor(
    private fb: FormBuilder,    // FormBuilder injizieren
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Formular aufbauen
    this.loginForm = this.fb.group({
      // 'username': [Startwert, [Validierungsregeln]]
      username: ['', [
        Validators.required,        // Pflichtfeld
        Validators.minLength(3)     // Mindestens 3 Zeichen
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6)
      ]]
    });
    
    // Wenn bereits eingeloggt → weiterleiten
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  // Getter für leichtere Template-Zugriffe
  get username() { return this.loginForm.get('username'); }
  get password() { return this.loginForm.get('password'); }

  onSubmit(): void {
    // Formular ungültig? → Abbrechen
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); // Fehler anzeigen
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value)
      .subscribe({
        next: () => {
          // Erfolgreich eingeloggt!
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          // Fehler vom Server
          this.errorMessage = err.error?.message || 'Login fehlgeschlagen';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
  }
}