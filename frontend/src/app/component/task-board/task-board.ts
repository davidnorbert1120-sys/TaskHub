import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { Subscription } from 'rxjs';
import { TaskService } from '../../service/task.service';
import { WebSocketService, TaskEventModel } from '../../service/websocket.service';
import { TaskListItemModel } from '../../model/task-list-item.model';
import { TaskStatus } from '../../model/task-status.model';

@Component({
  selector: 'app-task-board',
  standalone: false,
  templateUrl: './task-board.html',
  styleUrl: './task-board.css'
})
export class TaskBoard implements OnInit, OnChanges, OnDestroy {

  @Input() projectId!: number;

  todoTasks: TaskListItemModel[] = [];
  inProgressTasks: TaskListItemModel[] = [];
  doneTasks: TaskListItemModel[] = [];

  loading = false;
  globalError: string | null = null;

  private wsSubscription: Subscription | null = null;

  constructor(
    private taskService: TaskService,
    private webSocketService: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.webSocketService.connect();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId'] && this.projectId) {
      this.loadTasks();
      this.subscribeToTaskEvents();
    }
  }

  ngOnDestroy(): void {
    if (this.wsSubscription !== null) {
      this.wsSubscription.unsubscribe();
    }
  }

  loadTasks(): void {
    this.loading = true;
    this.globalError = null;
    console.log('TaskBoard: loading tasks for project', this.projectId);

    this.taskService.list(this.projectId).subscribe({
      next: (tasks) => {
        console.log('TaskBoard: loaded', tasks.length, 'tasks');
        this.todoTasks = this.sortByDueDate(tasks.filter(task => task.status === 'TODO'));
        this.inProgressTasks = this.sortByDueDate(tasks.filter(task => task.status === 'IN_PROGRESS'));
        this.doneTasks = this.sortByDueDate(tasks.filter(task => task.status === 'DONE'));
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('TaskBoard: failed to load tasks', error);
        this.loading = false;
        this.globalError = 'Nem sikerült betölteni a feladatokat.';
      }
    });
  }

  subscribeToTaskEvents(): void {
    if (this.wsSubscription !== null) {
      this.wsSubscription.unsubscribe();
    }
    this.wsSubscription = this.webSocketService.subscribeToProjectTasks(this.projectId).subscribe({
      next: (event) => {
        console.log('TaskBoard: handling event', event.type, 'for task', event.task.id);
        this.handleTaskEvent(event);
      }
    });
  }

  handleTaskEvent(event: TaskEventModel): void {
    if (event.type === 'STATUS_CHANGED') {
      this.removeTaskFromAllColumns(event.task.id);
      const listItem: TaskListItemModel = {
        id: event.task.id,
        title: event.task.title,
        status: event.task.status,
        priority: event.task.priority,
        dueDate: event.task.dueDate,
        assigneeUsername: event.task.assigneeUsername
      };
      this.addTaskToColumn(listItem);
    }
  }

  private removeTaskFromAllColumns(taskId: number): void {
    this.todoTasks = this.todoTasks.filter(task => task.id !== taskId);
    this.inProgressTasks = this.inProgressTasks.filter(task => task.id !== taskId);
    this.doneTasks = this.doneTasks.filter(task => task.id !== taskId);
  }

  private addTaskToColumn(task: TaskListItemModel): void {
    if (task.status === 'TODO') {
      this.todoTasks = this.sortByDueDate([...this.todoTasks, task]);
    } else if (task.status === 'IN_PROGRESS') {
      this.inProgressTasks = this.sortByDueDate([...this.inProgressTasks, task]);
    } else {
      this.doneTasks = this.sortByDueDate([...this.doneTasks, task]);
    }
  }

  onDrop(event: CdkDragDrop<TaskListItemModel[]>, targetStatus: TaskStatus): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const task = event.previousContainer.data[event.previousIndex];
      console.log('TaskBoard: dropping task', task.id, 'to', targetStatus);

      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      this.taskService.updateStatus(this.projectId, task.id, { status: targetStatus }).subscribe({
        next: (updated) => {
          console.log('TaskBoard: task status updated to', updated.status);
          task.status = updated.status;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskBoard: failed to update status', error);
          this.globalError = 'Nem sikerült megváltoztatni a státuszt. Frissítsd az oldalt.';
        }
      });
    }
  }

  goToCreate(): void {
    this.router.navigate(['/projects', this.projectId, 'tasks', 'new']);
  }

  goToDetail(task: TaskListItemModel): void {
    this.router.navigate(['/projects', this.projectId, 'tasks', task.id]);
  }

  private sortByDueDate(tasks: TaskListItemModel[]): TaskListItemModel[] {
    return [...tasks].sort((a, b) => {
      if (a.dueDate === null && b.dueDate === null) {
        return 0;
      } else if (a.dueDate === null) {
        return 1;
      } else if (b.dueDate === null) {
        return -1;
      } else {
        return a.dueDate.localeCompare(b.dueDate);
      }
    });
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
