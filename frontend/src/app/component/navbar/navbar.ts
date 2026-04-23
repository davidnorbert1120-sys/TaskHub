import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { UserItemModel } from '../../model/user-item.model';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  getCurrentUser(): UserItemModel | null {
    return this.authService.getCurrentUser();
  }

  logout(): void {
    console.log('Navbar.logout called');
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
