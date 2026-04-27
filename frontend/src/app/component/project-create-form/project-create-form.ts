import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProjectService } from '../../service/project.service';

@Component({
  selector: 'app-project-create-form',
  standalone: false,
  templateUrl: './project-create-form.html',
  styleUrl: './project-create-form.css'
})
export class ProjectCreateForm {

  projectForm: FormGroup;
  globalError: string | null = null;
  submitting = false;

  constructor(
    private formBuilder: FormBuilder,
    private projectService: ProjectService,
    private router: Router
  ) {
    this.projectForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(1000)]]
    });
  }

  onSubmit(): void {
    this.globalError = null;

    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
    } else {
      this.submitting = true;
      console.log('Submitting project create form with name:', this.projectForm.value.name);

      this.projectService.create(this.projectForm.value).subscribe({
        next: (project) => {
          console.log('Project created successfully with id:', project.id);
          this.submitting = false;
          this.router.navigate(['/projects', project.id]);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Project creation failed:', error);
          this.submitting = false;
          this.handleError(error);
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/projects']);
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      this.globalError = 'Kérlek ellenőrizd a mezőket.';
    } else if (error.status === 401) {
      this.globalError = 'Be kell jelentkezned.';
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }

  fieldHasError(fieldName: string, errorType: string): boolean {
    const field = this.projectForm.get(fieldName);
    return !!(field && field.touched && field.hasError(errorType));
  }
}
