// src/app/pages/tasks/tasks.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.css']
})
export class TasksComponent implements OnInit {

  allTasks: Task[] = [];
  filteredTasks: Task[] = [];
  isLoading = false;
  isSubmitting = false;       // ← NEU: Lade-Zustand beim Speichern

  // Modal-Steuerung
  showCreateModal = false;    // ← Existiert bereits
  showEditModal = false;      // ← NEU: für das Bearbeiten-Modal
  selectedTask: Task | null = null; // ← NEU: welcher Task wird bearbeitet

  // Filter
  searchControl = new FormControl('');
  selectedStatus = '';
  selectedPriority = '';

  // ← NEU: Das Formular für neue/bearbeitete Tasks
  taskForm!: FormGroup;

  constructor(
    private taskService: TaskService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.loadTasks();
    this.setupSearchFilter();
    this.initTaskForm();
  }

  // ────────────────────────────────────────
  // FORMULAR INITIALISIEREN
  // ────────────────────────────────────────
  initTaskForm(): void {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(2000)]],
      status:   ['TODO'],     // Standardwert: Offen
      priority: ['MEDIUM'],   // Standardwert: Mittel
      dueDate:  ['']
    });
  }

  // ────────────────────────────────────────
  // MODAL ÖFFNEN (NEU)
  // ────────────────────────────────────────
  openCreateModal(): void {
    this.selectedTask = null;
    this.taskForm.reset({       // Formular leeren und Standardwerte setzen
      title: '',
      description: '',
      status: 'TODO',
      priority: 'MEDIUM',
      dueDate: ''
    });
    this.showCreateModal = true;
  }

  // ────────────────────────────────────────
  // MODAL ÖFFNEN (BEARBEITEN)
  // ────────────────────────────────────────
  openEditModal(task: Task): void {
    this.selectedTask = task;
    // Formular mit vorhandenen Daten befüllen
    this.taskForm.patchValue({
      title:       task.title,
      description: task.description || '',
      status:      task.status,
      priority:    task.priority,
      dueDate:     task.dueDate || ''
    });
    this.showEditModal = true;
  }

  // ────────────────────────────────────────
  // MODAL SCHLIESSEN
  // ────────────────────────────────────────
  closeModal(): void {
    this.showCreateModal = false;
    this.showEditModal = false;
    this.selectedTask = null;
    this.taskForm.reset();
  }

  // ────────────────────────────────────────
  // TASK SPEICHERN (NEU oder BEARBEITEN)
  // ────────────────────────────────────────
  saveTask(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formData = this.taskForm.value;

    if (this.selectedTask) {
      // ── BEARBEITEN ──
      this.taskService.updateTask(this.selectedTask.id, formData)
        .subscribe({
          next: (updatedTask) => {
            // Task in der Liste ersetzen
            const index = this.allTasks.findIndex(t => t.id === this.selectedTask!.id);
            if (index !== -1) this.allTasks[index] = updatedTask;
            this.applyFilters();
            this.closeModal();
            this.isSubmitting = false;
          },
          error: () => { this.isSubmitting = false; }
        });
    } else {
      // ── NEU ERSTELLEN ──
      this.taskService.createTask(formData)
        .subscribe({
          next: (newTask) => {
            this.allTasks.unshift(newTask); // Am Anfang einfügen
            this.applyFilters();
            this.closeModal();
            this.isSubmitting = false;
          },
          error: () => { this.isSubmitting = false; }
        });
    }
  }

  // ────────────────────────────────────────
  // BESTEHENDE METHODEN (unverändert)
  // ────────────────────────────────────────

  trackById(index: number, task: Task): number {
    return task.id;
  }

  loadTasks(): void {
    this.isLoading = true;
    this.taskService.getTasks().subscribe({
      next: (tasks) => {
        this.allTasks = tasks;
        this.filteredTasks = tasks;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  setupSearchFilter(): void {
    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.applyFilters());
  }

  applyFilters(): void {
    let result = [...this.allTasks];
    const search = this.searchControl.value?.toLowerCase() || '';
    if (search) {
      result = result.filter(task =>
        task.title.toLowerCase().includes(search) ||
        task.description?.toLowerCase().includes(search)
      );
    }
    if (this.selectedStatus) {
      result = result.filter(task => task.status === this.selectedStatus);
    }
    if (this.selectedPriority) {
      result = result.filter(task => task.priority === this.selectedPriority);
    }
    this.filteredTasks = result;
  }

  resetFilters(): void {
    this.searchControl.reset('');
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.filteredTasks = [...this.allTasks];
  }

  updateStatus(task: Task, newStatus: string): void {
    this.taskService.updateTask(task.id, { status: newStatus as any })
      .subscribe(updatedTask => {
        const index = this.allTasks.findIndex(t => t.id === task.id);
        if (index !== -1) {
          this.allTasks[index] = updatedTask;
          this.applyFilters();
        }
      });
  }

  deleteTask(id: number): void {
    if (!confirm('Task wirklich löschen?')) return;
    this.taskService.deleteTask(id).subscribe(() => {
      this.allTasks = this.allTasks.filter(t => t.id !== id);
      this.applyFilters();
    });
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'TODO':        'bg-gray-100 text-gray-700',
      'IN_PROGRESS': 'bg-blue-100 text-blue-700',
      'DONE':        'bg-green-100 text-green-700'
    };
    return colors[status] || 'bg-gray-100 text-gray-700';
  }

  getPriorityColor(priority: string): string {
    const colors: Record<string, string> = {
      'LOW':    'bg-green-100 text-green-700',
      'MEDIUM': 'bg-yellow-100 text-yellow-700',
      'HIGH':   'bg-red-100 text-red-700'
    };
    return colors[priority] || '';
  }
}