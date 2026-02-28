import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent {
    form: any = {
        email: '',
        password: ''
    };
    isLoggedIn = false;
    isLoginFailed = false;
    errorMessage = '';
    isLoading = false;

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit(): void {
        if (this.authService.getToken()) {
            this.isLoggedIn = true;
            const user = this.authService.getUser();

            // Redirect based on role if already logged in
            if (user && user.role === 'ROLE_ADMIN') {
                this.router.navigate(['/admin/dashboard']);
            } else if (user && user.role === 'ROLE_STAFF') {
                this.router.navigate(['/staff/dashboard']);
            } else if (user && user.role === 'ROLE_STUDENT') {
                this.router.navigate(['/student/dashboard']);
            } else {
                // Corrupted state fallback
                this.authService.logout();
                this.isLoggedIn = false;
            }
        }
    }

    onSubmit(): void {
        const { email, password } = this.form;
        this.isLoading = true;

        this.authService.login({ email, password }).subscribe({
            next: data => {
                this.authService.saveToken(data.token);
                this.authService.saveUser(data);

                this.isLoginFailed = false;
                this.isLoggedIn = true;
                this.isLoading = false;

                // Redirect based on role
                if (data.role === 'ROLE_ADMIN') {
                    this.router.navigate(['/admin/dashboard']);
                } else if (data.role === 'ROLE_STAFF') {
                    this.router.navigate(['/staff/dashboard']);
                } else {
                    this.router.navigate(['/student/dashboard']);
                }
            },
            error: err => {
                this.errorMessage = err.error?.error || err.error?.message || 'Login failed. Please try again.';
                this.isLoginFailed = true;
                this.isLoading = false;
            }
        });
    }
}
