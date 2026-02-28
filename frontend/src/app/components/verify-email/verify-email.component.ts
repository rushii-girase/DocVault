import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-verify-email',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './verify-email.component.html',
    styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit {
    isSuccessful = false;
    isFailed = false;
    isLoading = true;
    message = '';

    constructor(
        private authService: AuthService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit(): void {
        const token = this.route.snapshot.queryParamMap.get('token');

        if (token) {
            this.authService.verifyEmail(token).subscribe({
                next: (res) => {
                    this.message = res.message;
                    this.isSuccessful = true;
                    this.isLoading = false;
                },
                error: (err) => {
                    this.message = err.error?.message || err.error?.error || 'Verification failed. Token may be invalid or expired.';
                    this.isFailed = true;
                    this.isLoading = false;
                }
            });
        } else {
            this.message = 'No verification token provided.';
            this.isFailed = true;
            this.isLoading = false;
        }
    }

    goToLogin(): void {
        this.router.navigate(['/login']);
    }
}
