// src/app/app.component.ts

import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  // Soll Sidebar/Navbar angezeigt werden?
  showLayout = false;

  // Diese Routen haben KEIN Layout (kein Header, keine Sidebar)
  private noLayoutRoutes = ['/login', '/register', '/not-found'];

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Bei jedem Seitenwechsel prüfen
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        // Prüft ob die aktuelle URL in der Ausnahmeliste ist
        this.showLayout = !this.noLayoutRoutes.some(route =>
          event.url.startsWith(route)
        );
        // .some() = "Gibt es IRGENDEINEN Eintrag wo die Bedingung true ist?"
      });
  }
}