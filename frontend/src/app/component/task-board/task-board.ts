import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TaskService } from '../../service/task.service';
import { TaskListItemModel } from '../../model/task-list-item.model';
import { TaskStatus } from '../../model/task-status.model';

@Component({
  selector: 'app-task-board',
  standalone: false,
  templateUrl: './task-board.html',
  styleUrl: './task-board.css'
})
export class TaskBoard implements OnChanges {

  @Input() projectId!: number;

  tasks: TaskListItemModel[] = [];
  loading = false;
  globalError: string | null = null;

  constructor(
    private taskService: TaskService,
    private router: Router
  ) {}

  goToCreate(): void {
    this.router.navigate(['/projects', this.projectId, 'tasks', 'new']);
  }

  goToDetail(task: TaskListItemModel): void {
    this.router.navigate(['/projects', this.projectId, 'tasks', task.id]);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId'] && this.projectId) {
      this.loadTasks();
    }
  }

  loadTasks(): void {
    this.loading = true;
    this.globalError = null;
    console.log('TaskBoard: loading tasks for project', this.projectId);

    this.taskService.list(this.projectId).subscribe({
      next: (tasks) => {
        console.log('TaskBoard: loaded', tasks.length, 'tasks');
        this.tasks = tasks;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('TaskBoard: failed to load tasks', error);
        this.loading = false;
        this.globalError = 'Nem sikerült betölteni a feladatokat.';
      }
    });
  }

  tasksByStatus(status: TaskStatus): TaskListItemModel[] {
    return this.tasks.filter(task => task.status === status);
  }

  moveTask(task: TaskListItemModel, newStatus: TaskStatus): void {
    if (task.status === newStatus) {
    } else {
      console.log('TaskBoard: moving task', task.id, 'to', newStatus);

      this.taskService.updateStatus(this.projectId, task.id, { status: newStatus }).subscribe({
        next: (updated) => {
          console.log('TaskBoard: task status updated');
          const index = this.tasks.findIndex(t => t.id === task.id);
          if (index !== -1) {
            this.tasks[index] = {
              ...this.tasks[index],
              status: updated.status
            };
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskBoard: failed to update status', error);
          this.globalError = 'Nem sikerült megváltoztatni a státuszt.';
        }
      });
    }
  }

  priorityBadgeClass(priority: string): string {
    if (priority === 'HIGH') {
      return 'bg-danger';
    } else if (priority === 'MEDIUM') {
      return 'bg-warning text-dark';
    } else {
      return 'bg-secondary';
    }
  }
}
