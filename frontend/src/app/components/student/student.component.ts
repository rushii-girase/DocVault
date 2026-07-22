import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { apiService } from '../../services/api.service';

@Component({
    selector: 'app-student',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './student.component.html',
    styleUrl: '../admin/admin.component.scss' // Same base styling
})
export class StudentComponent implements OnInit {
    user: any = null;
    activeTab: string = 'my-documents';
    isSidebarExpanded: boolean = false;
    documents: any[] = [];
    notifications: any[] = [];
    showProfileDetails: boolean = false;
    showEditModal: boolean = false;
    editProfileForm: any = { classLevel: '', division: '', rollNo: '' };
    classes = ['FE', 'SE', 'TE', 'BE'];
    editProfileMessage: string = '';

    uploadForm: any = {
        title: '',
        customTitle: '',
        file: null as File | null
    };
    uploadMessage = '';
    isUploading = false;

    constructor(private authService: AuthService, private apiService: apiService, private router: Router) { }

    ngOnInit(): void {
        if (!this.authService.getToken()) {
            this.router.navigate(['/login']);
            return;
        }
        this.user = this.authService.getUser();
        if (this.user.role !== 'ROLE_STUDENT') {
            this.router.navigate(['/login']);
        }
        this.loadData();
    }

    loadData() {
        this.apiService.getStudentDocuments(this.user.id).subscribe(res => this.documents = res);
        this.apiService.getNotifications().subscribe(res => this.notifications = res);
    }

    getRequiredDocuments(): string[] {
        const baseDocs = [
            'Aadhaar Card', 'Domicile Certificate', 'Income Certificate',
            'SSC', 'HSC', 'Previous Year Mark Sheet', 'Admission Receipt',
            'Ration Card', 'Undertaking/Declaration'
        ];
        if (['OBC', 'SC', 'ST'].includes(this.user?.caste)) {
            return [...baseDocs, 'Caste Certificate & Validity', 'Non-Creamy Layer Certificate'];
        }
        return baseDocs;
    }

    getMissingDocuments(): string[] {
        const uploadedTitles = this.documents.map(d => d.title);
        return this.getRequiredDocuments().filter(req => !uploadedTitles.includes(req));
    }

    onFileChange(event: any) {
        if (event.target.files.length > 0) {
            this.uploadForm.file = event.target.files[0];
        }
    }

    uploadDocument() {
        if (!this.uploadForm.file) return;

        this.isUploading = true;
        const formData = new FormData();
        formData.append('file', this.uploadForm.file);
        
        let finalTitle = this.uploadForm.title;
        if (finalTitle === 'CUSTOM') {
            finalTitle = this.uploadForm.customTitle;
        }
        formData.append('title', finalTitle);

        this.apiService.uploadDocument(formData).subscribe({
            next: (res) => {
                this.uploadMessage = 'Document uploaded successfully!';
                this.uploadForm = { title: '', customTitle: '', file: null };
                this.isUploading = false;
                this.loadData(); // refresh list
            },
            error: (err) => {
                this.uploadMessage = err.error?.message || 'Upload failed.';
                this.isUploading = false;
            }
        });
    }

    download(id: number, filename: string) {
        this.apiService.downloadDocument(id).subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            a.click();
            window.URL.revokeObjectURL(url);
        });
    }

    preview(id: number, filename: string) {
        this.apiService.previewDocument(id).subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            window.open(url, '_blank');
        });
    }

    deleteDoc(id: number) {
        if (confirm("Are you sure you want to delete this document? This cannot be undone.")) {
            this.apiService.deleteDocument(id).subscribe(() => {
                this.loadData();
            });
        }
    }

    markRead(id: number) {
        this.apiService.markNotificationAsRead(id).subscribe(() => this.loadData());
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    openEditModal(): void {
        const currentClassContent = this.user?.classLevel || '';
        this.editProfileForm = { 
            classLevel: currentClassContent === 'N/A' || !currentClassContent ? '' : currentClassContent, 
            division: this.user?.division || '',
            rollNo: this.user?.rollNo || ''
        };
        this.showEditModal = true;
        this.showProfileDetails = false;
        this.editProfileMessage = '';
    }

    closeEditModal(): void {
        this.showEditModal = false;
    }

    submitProfileEdit(): void {
        this.apiService.updateProfile(this.editProfileForm).subscribe({
            next: (res) => {
                this.authService.saveToken(res.token);
                this.authService.saveUser(res);
                this.user = this.authService.getUser();
                this.closeEditModal();
            },
            error: (err) => {
                this.editProfileMessage = err.error?.message || 'Failed to update profile. Please ensure inputs are valid.';
            }
        });
    }
}
