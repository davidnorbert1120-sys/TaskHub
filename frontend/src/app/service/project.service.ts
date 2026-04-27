import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectListItemModel } from '../model/project-list-item.model';
import { ProjectItemModel } from '../model/project-item.model';
import { ProjectCreateCommandModel } from '../model/project-create-command.model';
import { ProjectUpdateCommandModel } from '../model/project-update-command.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {

  BASE_URL = 'http://localhost:8080/projects';

  constructor(private httpClient: HttpClient) {}

  list(): Observable<ProjectListItemModel[]> {
    console.log('ProjectService.list called');
    return this.httpClient.get<ProjectListItemModel[]>(this.BASE_URL);
  }

  getById(id: number): Observable<ProjectItemModel> {
    console.log('ProjectService.getById called with id:', id);
    return this.httpClient.get<ProjectItemModel>(`${this.BASE_URL}/${id}`);
  }

  create(command: ProjectCreateCommandModel): Observable<ProjectItemModel> {
    console.log('ProjectService.create called with name:', command.name);
    return this.httpClient.post<ProjectItemModel>(this.BASE_URL, command);
  }

  update(id: number, command: ProjectUpdateCommandModel): Observable<ProjectItemModel> {
    console.log('ProjectService.update called for id:', id);
    return this.httpClient.put<ProjectItemModel>(`${this.BASE_URL}/${id}`, command);
  }

  delete(id: number): Observable<void> {
    console.log('ProjectService.delete called for id:', id);
    return this.httpClient.delete<void>(`${this.BASE_URL}/${id}`);
  }
}
