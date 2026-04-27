import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ProjectService } from '../../service/project.service';
import { ProjectItemModel } from '../../model/project-item.model';

@Component({
  selector: 'app-project-detail',
  standalone: false,
  templateUrl: './project-detail.html',
  styleUrl: './project-detail.css'
})
export class ProjectDetail implements OnInit {

  project: ProjectItemModel | null = null;
  projectId: number | null = null;

  editForm: FormGroup;
  editing = false;
  loading = false;
  submitting = false;
  globalError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private projectService: ProjectService
  ) {
    this.editForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(1000)]]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.projectId = Number(idParam);
      this.loadProject();
    }
  }

  loadProject(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.loading = true;
      this.globalError = null;
      console.log('ProjectDetail: loading project', this.projectId);

      this.projectService.getById(this.projectId).subscribe({
        next: (project) => {
          console.log('ProjectDetail: loaded project', project.name);
          this.project = project;
          this.editForm.patchValue({
            name: project.name,
            description: project.description
          });
          this.loading = false;
        },
        error: (error: HttpErrorResponse) => {
          console.error('ProjectDetail: failed to load', error);
          this.loading = false;
          this.handleLoadError(error);
        }
      });
    }
  }

  startEditing(): void {
    if (this.project) {
      this.editForm.patchValue({
        name: this.project.name,
        description: this.project.description
      });
      this.editing = true;
      this.globalError = null;
    }
  }

  cancelEditing(): void {
    this.editing = false;
    this.globalError = null;
  }

  onSave(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.globalError = null;

      if (this.editForm.invalid) {
        this.editForm.markAllAsTouched();
      } else {
        this.submitting = true;
        console.log('ProjectDetail: updating project', this.projectId);

        this.projectService.update(this.projectId, this.editForm.value).subscribe({
          next: (updated) => {
            console.log('ProjectDetail: updated successfully');
            this.project = updated;
            this.submitting = false;
            this.editing = false;
          },
          error: (error: HttpErrorResponse) => {
            console.error('ProjectDetail: update failed', error);
            this.submitting = false;
            this.handleSaveError(error);
          }
        });
      }
    }
  }


  onDelete(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      const confirmed = confirm(`Biztosan törlöd a(z) "${this.project?.name}" projektet?`);
      if (confirmed) {
        console.log('ProjectDetail: deleting project', this.projectId);

        this.projectService.delete(this.projectId).subscribe({
          next: () => {
            console.log('ProjectDetail: deleted successfully');
            this.router.navigate(['/projects']);
          },
          error: (error: HttpErrorResponse) => {
            console.error('ProjectDetail: delete failed', error);
            this.globalError = 'Nem sikerült törölni a projektet.';
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }

  fieldHasError(fieldName: string, errorType: string): boolean {
    const field = this.editForm.get(fieldName);
    return !!(field && field.touched && field.hasError(errorType));
  }

  private handleLoadError(error: HttpErrorResponse): void {
    if (error.status === 404) {
      this.globalError = 'A projekt nem található.';
    } else if (error.status === 403) {
      this.globalError = 'Nincs hozzáférésed ehhez a projekthez.';
    } else {
      this.globalError = 'Nem sikerült betölteni a projektet.';
    }
  }

  private handleSaveError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      this.globalError = 'Kérlek ellenőrizd a mezőket.';
    } else if (error.status === 404) {
      this.globalError = 'A projekt nem található.';
    } else if (error.status === 403) {
      this.globalError = 'Nincs jogosultságod a módosításhoz.';
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
