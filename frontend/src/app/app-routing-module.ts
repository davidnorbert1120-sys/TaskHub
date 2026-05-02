import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterForm } from './component/register-form/register-form';
import { LoginForm } from './component/login-form/login-form';
import { authGuard } from './guard/auth.guard';
import { ProjectList } from './component/project-list/project-list';
import { ProjectCreateForm } from './component/project-create-form/project-create-form';
import { ProjectDetail } from './component/project-detail/project-detail';
import {TaskCreateForm} from './component/task-create-form/task-create-form';
import {TaskDetail} from './component/task-detail/task-detail';

const routes: Routes = [
  { path: 'register', component: RegisterForm },
  { path: 'login', component: LoginForm },
  { path: 'projects/new', component: ProjectCreateForm, canActivate: [authGuard] },
  { path: 'projects/:id', component: ProjectDetail, canActivate: [authGuard] },
  { path: 'projects', component: ProjectList, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'projects/:projectId/tasks/new', component: TaskCreateForm, canActivate: [authGuard] },
  { path: 'projects/:projectId/tasks/:taskId', component: TaskDetail, canActivate: [authGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
