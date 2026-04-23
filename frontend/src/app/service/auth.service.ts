import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { UserItemModel } from '../model/user-item.model';
import { LoginCommandModel } from '../model/login-command.model';
import { RegisterCommandModel } from '../model/register-command.model';
import { AuthResponseModel } from '../model/auth-response.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  BASE_URL = 'http://localhost:8080/auth';

  private readonly TOKEN_KEY = 'taskhub_token';
  private readonly USER_KEY = 'taskhub_user';

  constructor(private http: HttpClient) {}

  register(command: RegisterCommandModel): Observable<UserItemModel> {
    console.log('AuthService.register called with username:', command.username);
    return this.http.post<UserItemModel>(`${this.BASE_URL}/register`, command);
  }

  login(command: LoginCommandModel): Observable<AuthResponseModel> {
    console.log('AuthService.login called with username:', command.username);
    return this.http.post<AuthResponseModel>(`${this.BASE_URL}/login`, command).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(response.user));
        console.log('Token and user saved to localStorage');
      })
    );
  }

  logout(): void {
    console.log('AuthService.logout called');
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): UserItemModel | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }
}
