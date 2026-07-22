import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './forgot-password.component.html',
    styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
    email = '';
    isSubmitted = false;
    isFailed = false;
    errorMessage = '';
    successMessage = '';
    isLoading = false;

    constructor(private authService: AuthService) { }

    onSubmit(): void {
        this.isLoading = true;
        this.isFailed = false;
        this.isSubmitted = false;

        this.authService.forgotPassword(this.email).subscribe({
            next: (data) => {
                this.isLoading = false;
                this.isSubmitted = true;
                this.successMessage = data.message || 'Password reset link has been sent to your email.';
            },
            error: (err) => {
                this.isLoading = false;
                this.isFailed = true;
                this.errorMessage = err.error?.message || 'Something went wrong. Please try again.';
            }
        });
    }
}
