import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterForm } from './component/register-form/register-form';
import { LoginForm } from './component/login-form/login-form';
import { authGuard } from './guard/auth.guard';
import { ProjectList } from './component/project-list/project-list';
import { ProjectCreateForm } from './component/project-create-form/project-create-form';
import { ProjectDetail } from './component/project-detail/project-detail';

const routes: Routes = [
  { path: 'register', component: RegisterForm },
  { path: 'login', component: LoginForm },
  { path: 'projects/new', component: ProjectCreateForm, canActivate: [authGuard] },
  { path: 'projects/:id', component: ProjectDetail, canActivate: [authGuard] },
  { path: 'projects', component: ProjectList, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
