import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './reset-password.component.html',
    styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
    form: any = {
        password: '',
        confirmPassword: ''
    };
    token = '';
    isSubmitted = false;
    isFailed = false;
    errorMessage = '';
    successMessage = '';
    isLoading = false;

    constructor(
        private authService: AuthService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get('token') || '';
        if (!this.token) {
            this.isFailed = true;
            this.errorMessage = 'Invalid request. Password reset token is missing.';
        }
    }

    onSubmit(): void {
        if (this.form.password !== this.form.confirmPassword) {
            this.isFailed = true;
            this.errorMessage = 'Passwords do not match.';
            return;
        }

        this.isLoading = true;
        this.isFailed = false;

        const payload = {
            token: this.token,
            password: this.form.password
        };

        this.authService.resetPassword(payload).subscribe({
            next: (data) => {
                this.isLoading = false;
                this.isSubmitted = true;
                this.successMessage = data.message || 'Your password has been successfully reset.';
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 3000);
            },
            error: (err) => {
                this.isLoading = false;
                this.isFailed = true;
                this.errorMessage = err.error?.message || 'Failed to reset password. The link may have expired.';
            }
        });
    }
}
