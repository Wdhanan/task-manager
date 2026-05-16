// src/app/pages/register/register.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {

  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Formular aufbauen – genau wie beim Login, nur mit mehr Feldern
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email:    ['', [Validators.required, Validators.email]],
      // Validators.email = prüft ob es eine gültige Email-Adresse ist
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName:['', [Validators.required]],
      lastName: ['', [Validators.required]]
    });

    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  // Getter für leichteren Zugriff im Template
  get username() { return this.registerForm.get('username'); }
  get email()    { return this.registerForm.get('email'); }
  get password() { return this.registerForm.get('password'); }
  get firstName(){ return this.registerForm.get('firstName'); }
  get lastName() { return this.registerForm.get('lastName'); }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registrierung fehlgeschlagen';
        this.isLoading = false;
      }
    });
  }
}