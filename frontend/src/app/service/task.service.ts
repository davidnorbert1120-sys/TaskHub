import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TaskListItemModel } from '../model/task-list-item.model';
import { TaskItemModel } from '../model/task-item.model';
import { TaskCreateCommandModel } from '../model/task-create-command.model';
import { TaskUpdateCommandModel } from '../model/task-update-command.model';
import { UpdateTaskStatusCommandModel } from '../model/update-task-status-command.model';

@Injectable({ providedIn: 'root' })
export class TaskService {

  BASE_URL = 'http://localhost:8080/projects';

  constructor(private httpClient: HttpClient) {}

  list(projectId: number): Observable<TaskListItemModel[]> {
    console.log('TaskService.list called for project:', projectId);
    return this.httpClient.get<TaskListItemModel[]>(`${this.BASE_URL}/${projectId}/tasks`);
  }

  getById(projectId: number, taskId: number): Observable<TaskItemModel> {
    console.log('TaskService.getById called for project:', projectId, 'task:', taskId);
    return this.httpClient.get<TaskItemModel>(`${this.BASE_URL}/${projectId}/tasks/${taskId}`);
  }

  create(projectId: number, command: TaskCreateCommandModel): Observable<TaskItemModel> {
    console.log('TaskService.create called for project:', projectId, 'title:', command.title);
    return this.httpClient.post<TaskItemModel>(`${this.BASE_URL}/${projectId}/tasks`, command);
  }

  update(projectId: number, taskId: number, command: TaskUpdateCommandModel): Observable<TaskItemModel> {
    console.log('TaskService.update called for project:', projectId, 'task:', taskId);
    return this.httpClient.put<TaskItemModel>(`${this.BASE_URL}/${projectId}/tasks/${taskId}`, command);
  }

  updateStatus(projectId: number, taskId: number, command: UpdateTaskStatusCommandModel): Observable<TaskItemModel> {
    console.log('TaskService.updateStatus called for project:', projectId, 'task:', taskId, 'status:', command.status);
    return this.httpClient.patch<TaskItemModel>(`${this.BASE_URL}/${projectId}/tasks/${taskId}/status`, command);
  }

  delete(projectId: number, taskId: number): Observable<void> {
    console.log('TaskService.delete called for project:', projectId, 'task:', taskId);
    return this.httpClient.delete<void>(`${this.BASE_URL}/${projectId}/tasks/${taskId}`);
  }
}
