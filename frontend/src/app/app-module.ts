import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbar } from './component/navbar/navbar';
import { RegisterForm } from './component/register-form/register-form';
import {ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { LoginForm } from './component/login-form/login-form';
import {AuthInterceptor} from './interceptor/auth.interceptor';
import { ProjectList } from './component/project-list/project-list';
import { ProjectCreateForm } from './component/project-create-form/project-create-form';
import { ProjectDetail } from './component/project-detail/project-detail';

@NgModule({
  declarations: [
    App,
    Navbar,
    RegisterForm,
    LoginForm,
    ProjectList,
    ProjectCreateForm,
    ProjectDetail
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
