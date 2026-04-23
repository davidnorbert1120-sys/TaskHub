import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {RegisterForm} from './component/register-form/register-form';

const routes: Routes = [
  {path: 'register', component: RegisterForm},
  {path: '', redirectTo: '/register', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
