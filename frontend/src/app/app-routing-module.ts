import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {RegisterForm} from './component/register-form/register-form';
import {LoginForm} from './component/login-form/login-form';
import {authGuard} from './guard/auth.guard';

const routes: Routes = [
  {path: 'register', component: RegisterForm},
  { path: 'login', component: LoginForm },
  { path: 'projects', component: LoginForm, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
