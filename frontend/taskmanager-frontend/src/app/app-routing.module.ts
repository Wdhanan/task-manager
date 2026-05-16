import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Alle Pages importieren
import { LoginComponent }     from './pages/login/login.component';
import { RegisterComponent }  from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { TasksComponent }     from './pages/tasks/tasks.component';
import { ProfileComponent }   from './pages/profile/profile.component';
import { NotFoundComponent }  from './pages/not-found/not-found.component';

// Den Guard importieren (Türsteher)
import { authGuard } from './guards/auth.guard';

const routes: Routes = [{ 
    path: '', 
    redirectTo: '/dashboard', 
    pathMatch: 'full' 
  },

  // Öffentliche Seiten (KEIN Login nötig)
  { path: 'login',    component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Geschützte Seiten (Login ERFORDERLICH)
  // canActivate: [authGuard] = "Nur rein lassen wenn eingeloggt"
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [authGuard]   // ← Der Türsteher!
  },
  { 
    path: 'tasks', 
    component: TasksComponent,
    canActivate: [authGuard] 
  },
  { 
    path: 'profile', 
    component: ProfileComponent,
    canActivate: [authGuard] 
  },
  
  // 404 - Seite nicht gefunden
  // "**" bedeutet: "Alles andere"
  { path: '**', component: NotFoundComponent }


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
