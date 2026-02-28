import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    {
        path: 'login',
        loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'verify-email',
        loadComponent: () => import('./components/verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
    },
    {
        path: 'admin/dashboard',
        loadComponent: () => import('./components/admin/admin.component').then(m => m.AdminComponent)
    },
    {
        path: 'staff/dashboard',
        loadComponent: () => import('./components/staff/staff.component').then(m => m.StaffComponent)
    },
    {
        path: 'student/dashboard',
        loadComponent: () => import('./components/student/student.component').then(m => m.StudentComponent)
    }
];
