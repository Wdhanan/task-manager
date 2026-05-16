// src/app/pages/dashboard/dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { User } from '../../models/user';

// Interface für die Statistik-Karten
interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;      // Tailwind Hintergrundfarbe
  textColor: string;  // Tailwind Textfarbe
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {

  currentUser: User | null = null;
  isLoading = true;

  // Die 4 Statistik-Karten
  stats: StatCard[] = [];

  constructor(
    private authService: AuthService,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    // Aktuellen User holen
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    this.loadStatistics();
  }

  loadStatistics(): void {
    this.isLoading = true;

    // Alle Tasks holen und Statistiken berechnen
    this.taskService.getTasks().subscribe({
      next: (tasks) => {
        const total      = tasks.length;
        const todo       = tasks.filter(t => t.status === 'TODO').length;
        const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS').length;
        const done       = tasks.filter(t => t.status === 'DONE').length;

        // Statistik-Karten aufbauen
        this.stats = [
          {
            title: 'Gesamt',
            value: total,
            icon: '📋',
            color: 'bg-blue-50',
            textColor: 'text-blue-600'
          },
          {
            title: 'Offen',
            value: todo,
            icon: '🕐',
            color: 'bg-yellow-50',
            textColor: 'text-yellow-600'
          },
          {
            title: 'In Bearbeitung',
            value: inProgress,
            icon: '⚡',
            color: 'bg-purple-50',
            textColor: 'text-purple-600'
          },
          {
            title: 'Erledigt',
            value: done,
            icon: '✅',
            color: 'bg-green-50',
            textColor: 'text-green-600'
          }
        ];
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  // Tageszeit-abhängige Begrüßung
  // Stell dir vor: Du schaust auf die Uhr und sagst
  // "Guten Morgen" oder "Guten Abend" je nach Zeit
  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Guten Morgen';
    if (hour < 18) return 'Guten Tag';
    return 'Guten Abend';
  }
}