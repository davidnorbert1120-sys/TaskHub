import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ProjectService } from '../../service/project.service';
import { ProjectMemberService } from '../../service/project-member.service';
import { ProjectItemModel } from '../../model/project-item.model';
import { ProjectMemberItemModel } from '../../model/project-member-item.model';
import { AddMemberCommandModel } from '../../model/add-member-command.model';

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

  members: ProjectMemberItemModel[] = [];
  membersLoading = false;
  membersError: string | null = null;

  addMemberForm: FormGroup;
  addingMember = false;
  addMemberError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private projectService: ProjectService,
    private projectMemberService: ProjectMemberService
  ) {
    this.editForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(1000)]]
    });
    this.addMemberForm = this.formBuilder.group({
      username: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam === null) {
      this.globalError = 'Hibás projekt azonosító.';
    } else {
      this.projectId = Number(idParam);
      this.loadProject();
      this.loadMembers();
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

  loadMembers(): void {
    if (this.projectId === null) {
      this.membersError = 'Hibás projekt azonosító.';
    } else {
      this.membersLoading = true;
      this.membersError = null;
      console.log('ProjectDetail: loading members for project', this.projectId);

      this.projectMemberService.list(this.projectId).subscribe({
        next: (members) => {
          console.log('ProjectDetail: loaded', members.length, 'members');
          this.members = members;
          this.membersLoading = false;
        },
        error: (error: HttpErrorResponse) => {
          console.error('ProjectDetail: failed to load members', error);
          this.membersLoading = false;
          this.membersError = 'Nem sikerült betölteni a tagokat.';
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

  onAddMember(): void {
    if (this.projectId === null) {
      this.addMemberError = 'Hibás projekt azonosító.';
    } else {
      this.addMemberError = null;

      if (this.addMemberForm.invalid) {
        this.addMemberForm.markAllAsTouched();
      } else {
        this.addingMember = true;
        const command: AddMemberCommandModel = this.addMemberForm.value;
        console.log('ProjectDetail: adding member', command.username);

        this.projectMemberService.add(this.projectId, command).subscribe({
          next: (newMember) => {
            console.log('ProjectDetail: member added successfully:', newMember.username);
            this.members = [...this.members, newMember];
            this.addMemberForm.reset();
            this.addingMember = false;
          },
          error: (error: HttpErrorResponse) => {
            console.error('ProjectDetail: failed to add member', error);
            this.addingMember = false;
            this.handleAddMemberError(error);
          }
        });
      }
    }
  }

  onRemoveMember(member: ProjectMemberItemModel): void {
    if (this.projectId === null) {
      this.membersError = 'Hibás projekt azonosító.';
    } else {
      const confirmed = confirm(`Biztosan eltávolítod ${member.username}-t a projektből?`);
      if (confirmed) {
        console.log('ProjectDetail: removing member', member.id);

        this.projectMemberService.remove(this.projectId, member.id).subscribe({
          next: () => {
            console.log('ProjectDetail: member removed successfully');
            this.members = this.members.filter(m => m.id !== member.id);
          },
          error: (error: HttpErrorResponse) => {
            console.error('ProjectDetail: failed to remove member', error);
            this.membersError = this.parseRemoveMemberError(error);
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

  isCurrentUserOwner(): boolean {
    if (this.project === null) {
      return false;
    } else {
      return this.project.ownerUsername === this.getCurrentUsername();
    }
  }

  private getCurrentUsername(): string | null {
    const userJson = localStorage.getItem('taskhub_user');
    if (userJson === null) {
      return null;
    } else {
      return JSON.parse(userJson).username;
    }
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

  private handleAddMemberError(error: HttpErrorResponse): void {
    if (error.status === 404 && error.error?.errorCode === 'USER_NOT_FOUND') {
      this.addMemberError = 'Nincs ilyen felhasználó.';
    } else if (error.status === 409) {
      this.addMemberError = 'Ez a felhasználó már tag.';
    } else if (error.status === 403) {
      this.addMemberError = 'Csak a projekt tulajdonosa adhat hozzá tagot.';
    } else if (error.status === 400) {
      this.addMemberError = 'Add meg a felhasználónevet.';
    } else {
      this.addMemberError = 'Váratlan hiba történt.';
    }
  }

  private parseRemoveMemberError(error: HttpErrorResponse): string {
    if (error.status === 400 && error.error?.errorCode === 'CANNOT_REMOVE_OWNER') {
      return 'A projekt tulajdonosát nem lehet eltávolítani.';
    } else if (error.status === 403) {
      return 'Csak a projekt tulajdonosa távolíthat el tagokat.';
    } else if (error.status === 404) {
      return 'A tag nem található.';
    } else {
      return 'Nem sikerült eltávolítani a tagot.';
    }
  }
}
