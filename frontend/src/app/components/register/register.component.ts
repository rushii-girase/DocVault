import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './register.component.html',
    styleUrl: './register.component.scss'
})
export class RegisterComponent {
    form: any = {
        name: '',
        email: '',
        password: '',
        mobileNo: '',
        caste: '',
        collegeName: '',
        course: '',
        classLevel: '',
        division: '',
        rollNo: ''
    };

    isSuccessful = false;
    isSignUpFailed = false;
    errorMessage = '';
    isLoading = false;

    colleges = [
        'KCTs Late G.N Sapkal College Of Engineering',
        'KCTs R. G. Sapkal College Of Pharmacy',
        'KCTs R. G. Sapkal Intitution Of Pharmacy',
        'KCTs K. R. Sapkal College Of Management Studies'
    ];

    castes = ['GENERAL', 'OBC', 'EBC', 'ST', 'SC'];
    classes = ['FE', 'SE', 'TE', 'BE'];

    availableCourses: string[] = [];

    constructor(private authService: AuthService, private router: Router) { }

    onCollegeChange() {
        this.form.course = ''; // Reset course on college change
        this.availableCourses = [];

        switch (this.form.collegeName) {
            case 'KCTs Late G.N Sapkal College Of Engineering':
                this.availableCourses = [
                    'E&TC ENGINEERING', 'COMPUTER ENGINEERING', 'AI&DS ENGINEERING',
                    'CIVIL ENGINEERING', 'MECHANICAL ENGINEERING', 'ELECTRICAL ENGINEERING'
                ];
                break;
            case 'KCTs R. G. Sapkal College Of Pharmacy':
                this.availableCourses = ['PHARMACY'];
                this.form.course = 'PHARMACY'; // Default specific selection
                break;
            case 'KCTs R. G. Sapkal Intitution Of Pharmacy':
                this.availableCourses = ['DIPLOMA', 'PHARMACY'];
                break;
            case 'KCTs K. R. Sapkal College Of Management Studies':
                this.availableCourses = ['MANAGEMENT OF BUSINESS ANALYTICS'];
                this.form.course = 'MANAGEMENT OF BUSINESS ANALYTICS'; // Default specific selection
                break;
        }
    }

    onSubmit(): void {
        this.isLoading = true;
        this.authService.registerStudent(this.form).subscribe({
            next: data => {
                this.isSuccessful = true;
                this.isSignUpFailed = false;
                this.isLoading = false;
            },
            error: err => {
                this.errorMessage = err.error?.error || err.error?.message || 'Registration failed.';
                this.isSignUpFailed = true;
                this.isLoading = false;
            }
        });
    }
}
