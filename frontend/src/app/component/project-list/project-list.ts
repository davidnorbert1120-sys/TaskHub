import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProjectService } from '../../service/project.service';
import { ProjectListItemModel } from '../../model/project-list-item.model';

@Component({
  selector: 'app-project-list',
  standalone: false,
  templateUrl: './project-list.html',
  styleUrl: './project-list.css'
})
export class ProjectList implements OnInit {

  projects: ProjectListItemModel[] = [];
  loading = false;
  globalError: string | null = null;

  constructor(
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.globalError = null;
    console.log('ProjectList: loading projects...');

    this.projectService.list().subscribe({
      next: (projects) => {
        console.log('ProjectList: received', projects.length, 'projects');
        this.projects = projects;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('ProjectList: failed to load', error);
        this.loading = false;
        this.globalError = 'Nem sikerült betölteni a projekteket.';
      }
    });
  }

  goToCreate(): void {
    this.router.navigate(['/projects/new']);
  }

  goToDetail(id: number): void {
    this.router.navigate(['/projects', id]);
  }
}
