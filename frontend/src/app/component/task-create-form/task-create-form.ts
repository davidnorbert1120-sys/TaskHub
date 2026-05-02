import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TaskService } from '../../service/task.service';
import { ProjectMemberService } from '../../service/project-member.service';
import { ProjectMemberItemModel } from '../../model/project-member-item.model';
import { TaskCreateCommandModel } from '../../model/task-create-command.model';

@Component({
  selector: 'app-task-create-form',
  standalone: false,
  templateUrl: './task-create-form.html',
  styleUrl: './task-create-form.css'
})
export class TaskCreateForm implements OnInit {

  projectId: number | null = null;
  taskForm: FormGroup;
  members: ProjectMemberItemModel[] = [];
  submitting = false;
  globalError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private taskService: TaskService,
    private projectMemberService: ProjectMemberService
  ) {
    this.taskForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(2000)]],
      priority: ['MEDIUM', [Validators.required]],
      status: ['TODO', [Validators.required]],
      dueDate: [''],
      assigneeUsername: ['']
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('projectId');
    if (idParam === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.projectId = Number(idParam);
      this.loadMembers();
    }
  }

  loadMembers(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      console.log('TaskCreateForm: loading members for project', this.projectId);
      this.projectMemberService.list(this.projectId).subscribe({
        next: (members) => {
          console.log('TaskCreateForm: loaded', members.length, 'members');
          this.members = members;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskCreateForm: failed to load members', error);
          this.globalError = 'Nem sikerült betölteni a tagokat.';
        }
      });
    }
  }

  onSubmit(): void {
    if (this.projectId === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.globalError = null;

      if (this.taskForm.invalid) {
        this.taskForm.markAllAsTouched();
      } else {
        this.submitting = true;
        const command = this.buildCommand();
        console.log('TaskCreateForm: submitting task with title:', command.title);

        this.taskService.create(this.projectId, command).subscribe({
          next: (task) => {
            console.log('TaskCreateForm: task created with id:', task.id);
            this.submitting = false;
            this.router.navigate(['/projects', this.projectId]);
          },
          error: (error: HttpErrorResponse) => {
            console.error('TaskCreateForm: failed to create task', error);
            this.submitting = false;
            this.handleError(error);
          }
        });
      }
    }
  }

  onCancel(): void {
    this.router.navigate(['/projects', this.projectId]);
  }

  fieldHasError(fieldName: string, errorType: string): boolean {
    const field = this.taskForm.get(fieldName);
    return !!(field && field.touched && field.hasError(errorType));
  }

  private buildCommand(): TaskCreateCommandModel {
    const value = this.taskForm.value;
    const command: TaskCreateCommandModel = {
      title: value.title,
      priority: value.priority,
      status: value.status
    };
    if (value.description) {
      command.description = value.description;
    }
    if (value.dueDate) {
      command.dueDate = value.dueDate;
    }
    if (value.assigneeUsername) {
      command.assigneeUsername = value.assigneeUsername;
    }
    return command;
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.errorCode === 'INVALID_ASSIGNEE') {
      this.globalError = 'A kiválasztott felhasználó nem tagja a projektnek.';
    } else if (error.status === 400) {
      this.globalError = 'Kérlek ellenőrizd a mezőket.';
    } else if (error.status === 403) {
      this.globalError = 'Nincs jogosultságod a feladat létrehozásához.';
    } else if (error.status === 404) {
      this.globalError = 'A projekt nem található.';
    } else {
      this.globalError = 'Váratlan hiba történt. Próbáld újra.';
    }
  }
}
