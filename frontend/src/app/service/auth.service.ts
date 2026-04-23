import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserItemModel } from '../model/user-item.model';

export interface RegisterCommand {
  username: string;
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) {}

  register(command: RegisterCommand): Observable<UserItemModel> {
    console.log('AuthService.register called with username:', command.username);
    return this.http.post<UserItemModel>(`${this.apiUrl}/register`, command);
  }
}
