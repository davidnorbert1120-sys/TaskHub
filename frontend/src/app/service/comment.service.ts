import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentItemModel } from '../model/comment-item.model';
import { CommentCreateCommandModel } from '../model/comment-create-command.model';

@Injectable({ providedIn: 'root' })
export class CommentService {

  BASE_URL = 'http://localhost:8080/projects';

  constructor(private httpClient: HttpClient) {}

  list(projectId: number, taskId: number): Observable<CommentItemModel[]> {
    console.log('CommentService.list called for project:', projectId, 'task:', taskId);
    return this.httpClient.get<CommentItemModel[]>(
      `${this.BASE_URL}/${projectId}/tasks/${taskId}/comments`
    );
  }

  add(projectId: number, taskId: number, command: CommentCreateCommandModel): Observable<CommentItemModel> {
    console.log('CommentService.add called for project:', projectId, 'task:', taskId);
    return this.httpClient.post<CommentItemModel>(
      `${this.BASE_URL}/${projectId}/tasks/${taskId}/comments`,
      command
    );
  }

  delete(projectId: number, taskId: number, commentId: number): Observable<void> {
    console.log('CommentService.delete called for comment:', commentId);
    return this.httpClient.delete<void>(
      `${this.BASE_URL}/${projectId}/tasks/${taskId}/comments/${commentId}`
    );
  }
}
