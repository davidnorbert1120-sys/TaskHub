import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CommentService } from '../../service/comment.service';
import { CommentItemModel } from '../../model/comment-item.model';

@Component({
  selector: 'app-task-comments',
  standalone: false,
  templateUrl: './task-comments.html',
  styleUrl: './task-comments.css'
})
export class TaskComments implements OnChanges {

  @Input() projectId!: number;
  @Input() taskId!: number;
  @Input() projectOwnerUsername!: string;

  comments: CommentItemModel[] = [];
  loading = false;
  globalError: string | null = null;

  commentForm: FormGroup;
  submitting = false;
  submitError: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private commentService: CommentService
  ) {
    this.commentForm = this.formBuilder.group({
      content: ['', [Validators.required, Validators.maxLength(2000)]]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['projectId'] || changes['taskId']) && this.projectId && this.taskId) {
      this.loadComments();
    }
  }

  loadComments(): void {
    this.loading = true;
    this.globalError = null;
    console.log('TaskComments: loading comments for task', this.taskId);

    this.commentService.list(this.projectId, this.taskId).subscribe({
      next: (comments) => {
        console.log('TaskComments: loaded', comments.length, 'comments');
        this.comments = comments;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('TaskComments: failed to load', error);
        this.loading = false;
        this.globalError = 'Nem sikerült betölteni a hozzászólásokat.';
      }
    });
  }

  onSubmit(): void {
    this.submitError = null;

    if (this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
    } else {
      this.submitting = true;
      const command = { content: this.commentForm.value.content };
      console.log('TaskComments: adding comment to task', this.taskId);

      this.commentService.add(this.projectId, this.taskId, command).subscribe({
        next: (newComment) => {
          console.log('TaskComments: comment added with id:', newComment.id);
          this.comments = [...this.comments, newComment];
          this.commentForm.reset();
          this.submitting = false;
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskComments: failed to add', error);
          this.submitting = false;
          this.handleSubmitError(error);
        }
      });
    }
  }

  onDelete(comment: CommentItemModel): void {
    const confirmed = confirm('Biztosan törlöd ezt a hozzászólást?');
    if (confirmed) {
      console.log('TaskComments: deleting comment', comment.id);

      this.commentService.delete(this.projectId, this.taskId, comment.id).subscribe({
        next: () => {
          console.log('TaskComments: comment deleted');
          this.comments = this.comments.filter(c => c.id !== comment.id);
        },
        error: (error: HttpErrorResponse) => {
          console.error('TaskComments: failed to delete', error);
          this.globalError = this.parseDeleteError(error);
        }
      });
    }
  }

  canDelete(comment: CommentItemModel): boolean {
    const currentUser = this.getCurrentUsername();
    if (currentUser === null) {
      return false;
    } else {
      return comment.authorUsername === currentUser || this.projectOwnerUsername === currentUser;
    }
  }

  fieldHasError(fieldName: string, errorType: string): boolean {
    const field = this.commentForm.get(fieldName);
    return !!(field && field.touched && field.hasError(errorType));
  }

  private getCurrentUsername(): string | null {
    const userJson = localStorage.getItem('taskhub_user');
    if (userJson === null) {
      return null;
    } else {
      return JSON.parse(userJson).username;
    }
  }

  private handleSubmitError(error: HttpErrorResponse): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      this.submitError = 'A tartalom nem lehet üres.';
    } else if (error.status === 403) {
      this.submitError = 'Nincs jogosultságod hozzászólni.';
    } else if (error.status === 404) {
      this.submitError = 'A feladat nem található.';
    } else {
      this.submitError = 'Váratlan hiba történt.';
    }
  }

  private parseDeleteError(error: HttpErrorResponse): string {
    if (error.status === 403) {
      return 'Csak a saját hozzászólásodat törölheted (vagy ha te vagy a projekt tulajdonosa).';
    } else if (error.status === 404) {
      return 'A hozzászólás nem található.';
    } else {
      return 'Nem sikerült törölni a hozzászólást.';
    }
  }
}
