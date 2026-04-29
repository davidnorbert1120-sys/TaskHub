import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectMemberItemModel } from '../model/project-member-item.model';
import { AddMemberCommandModel } from '../model/add-member-command.model';

@Injectable({ providedIn: 'root' })
export class ProjectMemberService {

  BASE_URL = 'http://localhost:8080/projects';

  constructor(private httpClient: HttpClient) {}

  list(projectId: number): Observable<ProjectMemberItemModel[]> {
    console.log('ProjectMemberService.list called for project:', projectId);
    return this.httpClient.get<ProjectMemberItemModel[]>(`${this.BASE_URL}/${projectId}/members`);
  }

  add(projectId: number, command: AddMemberCommandModel): Observable<ProjectMemberItemModel> {
    console.log('ProjectMemberService.add called for project:', projectId, 'username:', command.username);
    return this.httpClient.post<ProjectMemberItemModel>(`${this.BASE_URL}/${projectId}/members`, command);
  }

  remove(projectId: number, memberId: number): Observable<void> {
    console.log('ProjectMemberService.remove called for project:', projectId, 'member:', memberId);
    return this.httpClient.delete<void>(`${this.BASE_URL}/${projectId}/members/${memberId}`);
  }
}
