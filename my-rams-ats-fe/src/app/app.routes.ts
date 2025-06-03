import { Routes } from '@angular/router';

// Authentication components
import { LoginComponent } from './components/auth/login/login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { ForgotPasswordComponent } from './components/auth/forgot-password/forgot-password.component';

// Layout components
import { AdminLayoutComponent } from './components/layout/admin-layout/admin-layout.component';
import { UserLayoutComponent } from './components/layout/user-layout/user-layout.component';
import { PublicLayoutComponent } from './components/layout/public-layout/public-layout.component';

// Dashboard components
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { UserDashboardComponent } from './components/dashboard/user-dashboard/user-dashboard.component';

// Job components
import { JobFormComponent } from './components/jobs/job-form/job-form.component';
import { JobListComponent } from './components/jobs/job-list/job-list.component';
import { JobViewComponent } from './components/jobs/job-view/job-view.component';
import { PublicJobListComponent } from './components/jobs/public-job-list/public-job-list.component';
import { PublicJobViewComponent } from './components/jobs/public-job-view/public-job-view.component';

// Master data components
import { DepartmentManagementComponent } from './components/master-data/department-management/department-management.component';
import { JobFunctionManagementComponent } from './components/master-data/job-function-management/job-function-management.component';
import { JobTypeManagementComponent } from './components/master-data/job-type-management/job-type-management.component';
import { LocationManagementComponent } from './components/master-data/location-management/location-management.component';
import { ExperienceLevelManagementComponent } from './components/master-data/experience-level-management/experience-level-management.component';
import { RoleManagementComponent } from './components/master-data/role-management/role-management.component';
import { MenuManagementComponent } from './components/master-data/menu-management/menu-management.component';

// User and contact components
import { UserManagementComponent } from './components/users/user-management/user-management.component';
import { ContactMessagesComponent } from './components/contact/contact-messages/contact-messages.component';
import { ContactFormComponent } from './components/contact/contact-form/contact-form.component';
import { LandingPageComponent } from './components/public/landing-page/landing-page.component';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { UserGuard } from './guards/user.guard';

export const routes: Routes = [
  // Public routes (no authentication required)
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', component: LandingPageComponent },
      { path: 'jobs', component: PublicJobListComponent },
      { path: 'jobs/:id', component: PublicJobViewComponent },
      { path: 'contact', component: ContactFormComponent },
    ]
  },
  
  // Authentication routes (public)
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  
  // Admin routes with layout
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      
      // Job Management
      { path: 'jobs', component: JobListComponent },
      { path: 'jobs/create', component: JobFormComponent },
      { path: 'jobs/:id', component: JobViewComponent },
      { path: 'jobs/:id/edit', component: JobFormComponent },
      
      // Master Data Management
      { path: 'master-data/departments', component: DepartmentManagementComponent },
      { path: 'master-data/job-functions', component: JobFunctionManagementComponent },
      { path: 'master-data/job-types', component: JobTypeManagementComponent },
      { path: 'master-data/locations', component: LocationManagementComponent },
      { path: 'master-data/experience-levels', component: ExperienceLevelManagementComponent },
      { path: 'master-data/roles', component: RoleManagementComponent },
      { path: 'master-data/menus', component: MenuManagementComponent },
      
      // User Management
      { path: 'users', component: UserManagementComponent },
      
      // Contact Messages
      { path: 'contact-messages', component: ContactMessagesComponent },
    ]
  },

  // User routes with layout
  {
    path: 'user',
    component: UserLayoutComponent,
    canActivate: [AuthGuard, UserGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: UserDashboardComponent },
      { path: 'jobs', component: PublicJobListComponent },
      { path: 'jobs/:id', component: PublicJobViewComponent },
      { path: 'profile', loadComponent: () => import('./components/user/profile/profile.component').then(m => m.ProfileComponent) },
    ]
  },
  
  // Wildcard route
  { path: '**', redirectTo: '/' }
];
