import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-register-form',
  standalone: false,
  templateUrl: './register-form.html',
  styleUrl: './register-form.css'
})
export class RegisterForm {

  registerForm: FormGroup;
  fieldErrors: { [key: string]: string } = {};
  globalError: string | null = null;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.pattern(/^\S+$/), Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern(/^\S+$/), Validators.minLength(8)]]
    });
  }

  onSubmit(): void {
    this.fieldErrors = {};
    this.globalError = null;

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
    } else {
      this.submitting = true;
      console.log('Submitting register form:', this.registerForm.value);

      this.authService.register(this.registerForm.value).subscribe({
        next: (user) => {
          console.log('Registration successful:', user);
          this.submitting = false;
          this.router.navigate(['/login']);
        },
        error: (err: HttpErrorResponse) => {
          console.error('Registration failed:', err);
          this.submitting = false;
          this.handleError(err);
        }
      });
    }
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === 400 && err.error?.fieldErrors) {
      for (const fe of err.error.fieldErrors) {
        this.fieldErrors[fe.field] = fe.message;
      }
    } else if (err.status === 409 && err.error?.errorCode) {
      if (err.error.errorCode === 'USERNAME_ALREADY_EXISTS') {
        this.fieldErrors['username'] = 'Ez a felhasználónév már foglalt.';
      } else if (err.error.errorCode === 'EMAIL_ALREADY_EXISTS') {
        this.fieldErrors['email'] = 'Ez az email cím már foglalt.';
      } else {
        this.globalError = err.error.error ?? 'Ismeretlen hiba.';
      }
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
