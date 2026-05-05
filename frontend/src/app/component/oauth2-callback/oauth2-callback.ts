import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { UserItemModel } from '../../model/user-item.model';

@Component({
  selector: 'app-oauth2-callback',
  standalone: false,
  templateUrl: './oauth2-callback.html',
  styleUrl: './oauth2-callback.css'
})
export class OAuth2Callback implements OnInit {

  errorMessage: string | null = null;

  private readonly TOKEN_KEY = 'taskhub_token';
  private readonly USER_KEY = 'taskhub_user';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private httpClient: HttpClient
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token === null) {
      console.error('OAuth2Callback: no token in query params');
      this.errorMessage = 'A bejelentkezés nem sikerült. Próbáld újra.';
    } else {
      console.log('OAuth2Callback: token received, saving and fetching user');
      localStorage.setItem(this.TOKEN_KEY, token);

      this.httpClient.get<UserItemModel>(`${environment.apiUrl}/users/me`, {
        headers: { Authorization: `Bearer ${token}` }
      }).subscribe({
        next: (user) => {
          console.log('OAuth2Callback: user fetched:', user.username);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this.router.navigate(['/projects']);
        },
        error: (error) => {
          console.error('OAuth2Callback: failed to fetch user', error);
          localStorage.removeItem(this.TOKEN_KEY);
          this.errorMessage = 'Nem sikerült betölteni a felhasználói adatokat.';
        }
      });
    }
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
