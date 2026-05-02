import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TaskService } from '../../service/task.service';
import { ProjectMemberService } from '../../service/project-member.service';
import { ProjectService } from '../../service/project.service';
import { TaskItemModel } from '../../model/task-item.model';
import { ProjectMemberItemModel } from '../../model/project-member-item.model';


@Component({
  selector: 'app-task-detail',
  standalone: false,
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.css'
})
export class TaskDetail implements OnInit {

  task: TaskItemModel | null = null;
  projectId: number | null = null;
  taskId: number | null = null;
  members: ProjectMemberItemModel[] = [];
  projectOwnerUsername: string = '';

  editForm: FormGroup;
  editing = false;
  loading = false;
  submitting = false;
  globalError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private taskService: TaskService,
    private projectMemberService: ProjectMemberService,
    private projectService: ProjectService
  ) {
    this.editForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(2000)]],
      priority: ['MEDIUM', [Validators.required]],
      status: ['TODO', [Validators.required]],
      dueDate: [''],
      assigneeUsername: ['']
    });
  }

  ngOnInit(): void {
    const projectIdParam = this.route.snapshot.paramMap.get('projectId');
    const taskIdParam = this.route.snapshot.paramMap.get('taskId');
    if (projectIdParam === null || taskIdParam === null) {
      this.globalError = 'Hibás azonosító.';
    } else {
      this.projectId = Number(projectIdParam);
      this.taskId = Number(taskIdParam);
      this.loadTask();
      this.loadMembers();
      this.loadProjectOwner();
    }
  }

  loadProjectOwner(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.projectService.getById(this.projectId).subscribe({
        next: (project) => {
          this.projectOwnerUsername = project.ownerUsername;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskDetail: failed to load project for owner info', error);
        }
      });
    }
  }

  loadTask(): void {
    if (this.projectId === null || this.taskId === null) {
      this.globalError = 'Hibás azonosító.';
    } else {
      this.loading = true;
      this.globalError = null;
      console.log('TaskDetail: loading task', this.taskId);

      this.taskService.getById(this.projectId, this.taskId).subscribe({
        next: (task) => {
          console.log('TaskDetail: loaded task', task.title);
          this.task = task;
          this.editForm.patchValue({
            title: task.title,
            description: task.description,
            priority: task.priority,
            status: task.status,
            dueDate: task.dueDate,
            assigneeUsername: task.assigneeUsername
          });
          this.loading = false;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskDetail: failed to load', error);
          this.loading = false;
          this.handleLoadError(error);
        }
      });
    }
  }

  loadMembers(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.projectMemberService.list(this.projectId).subscribe({
        next: (members) => {
          this.members = members;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskDetail: failed to load members', error);
        }
      });
    }
  }

  startEditing(): void {
    if (this.task) {
      this.editForm.patchValue({
        title: this.task.title,
        description: this.task.description,
        priority: this.task.priority,
        status: this.task.status,
        dueDate: this.task.dueDate,
        assigneeUsername: this.task.assigneeUsername
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
    if (this.projectId === null || this.taskId === null) {
      this.globalError = 'Hibás azonosító.';
    } else {
      this.globalError = null;

      if (this.editForm.invalid) {
        this.editForm.markAllAsTouched();
      } else {
        this.submitting = true;
        const value = this.editForm.value;
        const command = {
          title: value.title,
          description: value.description || undefined,
          priority: value.priority,
          status: value.status,
          dueDate: value.dueDate || undefined,
          assigneeUsername: value.assigneeUsername || undefined
        };
        console.log('TaskDetail: updating task', this.taskId);

        this.taskService.update(this.projectId, this.taskId, command).subscribe({
          next: (updated) => {
            console.log('TaskDetail: updated successfully');
            this.task = updated;
            this.submitting = false;
            this.editing = false;
          },
          error: (error: HttpErrorResponse) => {
            console.error('TaskDetail: update failed', error);
            this.submitting = false;
            this.handleSaveError(error);
          }
        });
      }
    }
  }

  onDelete(): void {
    if (this.projectId === null || this.taskId === null) {
      this.globalError = 'Hibás azonosító.';
    } else {
      const confirmed = confirm(`Biztosan törlöd a(z) "${this.task?.title}" feladatot?`);
      if (confirmed) {
        console.log('TaskDetail: deleting task', this.taskId);

        this.taskService.delete(this.projectId, this.taskId).subscribe({
          next: () => {
            console.log('TaskDetail: deleted successfully');
            this.router.navigate(['/projects', this.projectId]);
          },
          error: (error: HttpErrorResponse) => {
            console.error('TaskDetail: delete failed', error);
            this.globalError = 'Nem sikerült törölni a feladatot.';
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/projects', this.projectId]);
  }

  fieldHasError(fieldName: string, errorType: string): boolean {
    const field = this.editForm.get(fieldName);
    return !!(field && field.touched && field.hasError(errorType));
  }

  priorityBadgeClass(priority: string): string {
    if (priority === 'HIGH') {
      return 'bg-danger';
    } else if (priority === 'MEDIUM') {
      return 'bg-warning text-dark';
    } else {
      return 'bg-secondary';
    }
  }

  statusBadgeClass(status: string): string {
    if (status === 'DONE') {
      return 'bg-success';
    } else if (status === 'IN_PROGRESS') {
      return 'bg-primary';
    } else {
      return 'bg-secondary';
    }
  }

  statusLabel(status: string): string {
    if (status === 'TODO') {
      return 'Tennivaló';
    } else if (status === 'IN_PROGRESS') {
      return 'Folyamatban';
    } else {
      return 'Kész';
    }
  }

  private handleLoadError(error: HttpErrorResponse): void {
    if (error.status === 404) {
      this.globalError = 'A feladat nem található.';
    } else if (error.status === 403) {
      this.globalError = 'Nincs hozzáférésed ehhez a projekthez.';
    } else {
      this.globalError = 'Nem sikerült betölteni a feladatot.';
    }
  }

  private handleSaveError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.errorCode === 'INVALID_ASSIGNEE') {
      this.globalError = 'A kiválasztott felhasználó nem tagja a projektnek.';
    } else if (error.status === 400) {
      this.globalError = 'Kérlek ellenőrizd a mezőket.';
    } else if (error.status === 404) {
      this.globalError = 'A feladat nem található.';
    } else if (error.status === 403) {
      this.globalError = 'Nincs jogosultságod a módosításhoz.';
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
