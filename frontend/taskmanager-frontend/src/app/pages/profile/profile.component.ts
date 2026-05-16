// src/app/pages/profile/profile.component.ts

import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {

  currentUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  // Ersten Buchstaben des Usernames groß → für den Avatar-Kreis
  getInitials(): string {
    if (!this.currentUser?.username) return '?';
    return this.currentUser.username.charAt(0).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}