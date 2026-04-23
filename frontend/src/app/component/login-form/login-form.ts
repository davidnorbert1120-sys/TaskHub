import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-login-form',
  standalone: false,
  templateUrl: './login-form.html',
  styleUrl: './login-form.css'
})
export class LoginForm {

  loginForm: FormGroup;
  globalError: string | null = null;
  submitting = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    this.globalError = null;

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
    } else {
      this.submitting = true;
      console.log('Submitting login form for username:', this.loginForm.value.username);

      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          console.log('Login successful for user:', response.user.username);
          this.submitting = false;
          this.router.navigate(['/']);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Login failed:', error);
          this.submitting = false;
          this.handleError(error);
        }
      });
    }
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 401 && error.error?.errorCode === 'INVALID_CREDENTIALS') {
      this.globalError = 'Hibás felhasználónév vagy jelszó.';
    } else if (error.status === 400 && error.error?.fieldErrors) {
      this.globalError = 'Kérlek töltsd ki az összes mezőt.';
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
