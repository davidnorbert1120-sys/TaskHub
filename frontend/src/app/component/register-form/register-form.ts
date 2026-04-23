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
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.formBuilder.group({
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
        error: (error: HttpErrorResponse) => {
          console.error('Registration failed:', error);
          this.submitting = false;
          this.handleError(error);
        }
      });
    }
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      for (const fieldError of error.error.fieldErrors) {
        this.fieldErrors[fieldError.field] = fieldError.message;
      }
    } else if (error.status === 409 && error.error?.errorCode) {
      if (error.error.errorCode === 'USERNAME_ALREADY_EXISTS') {
        this.fieldErrors['username'] = 'Ez a felhasználónév már foglalt.';
      } else if (error.error.errorCode === 'EMAIL_ALREADY_EXISTS') {
        this.fieldErrors['email'] = 'Ez az email cím már foglalt.';
      } else {
        this.globalError = error.error.error ?? 'Ismeretlen hiba.';
      }
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
