import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Task } from '../models/task';

export interface TaskFilter {
  status?: string;
  priority?: string;
  search?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
private apiUrl = `${environment.apiUrl}/tasks`;

  constructor(private http: HttpClient) { }

  // ────────────────────────────────────────
  // ALLE TASKS HOLEN (mit optionalem Filter)
  // ────────────────────────────────────────
  getTasks(filter?: TaskFilter): Observable<Task[]> {
    // HttpParams = URL-Parameter bauen
    // Ergebnis: /api/tasks?status=TODO&priority=HIGH
    let params = new HttpParams();
    
    if (filter?.status)   params = params.set('status',   filter.status);
    if (filter?.priority) params = params.set('priority', filter.priority);
    if (filter?.search)   params = params.set('search',   filter.search);

    return this.http.get<Task[]>(this.apiUrl, { params });
    //                    ↑ TypeScript sagt: "Die Antwort ist ein Array von Task-Objekten"
  }

  // ────────────────────────────────────────
  // EINEN TASK HOLEN
  // ────────────────────────────────────────
  getTask(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
    // → GET http://localhost:8080/api/tasks/5
  }

  // ────────────────────────────────────────
  // TASK ERSTELLEN
  // ────────────────────────────────────────
  createTask(task: Partial<Task>): Observable<Task> {
    // Partial<Task> = "Ein Task-Objekt, aber alle Felder sind optional"
    return this.http.post<Task>(this.apiUrl, task);
    // → POST http://localhost:8080/api/tasks
    //   Body: { title: "...", description: "..." }
  }

  // ────────────────────────────────────────
  // TASK AKTUALISIEREN
  // ────────────────────────────────────────
  updateTask(id: number, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
    // → PUT http://localhost:8080/api/tasks/5
  }

  // ────────────────────────────────────────
  // TASK LÖSCHEN
  // ────────────────────────────────────────
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
    // → DELETE http://localhost:8080/api/tasks/5
  }

  
}
