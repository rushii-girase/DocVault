import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { apiService } from '../../services/api.service';

@Component({
    selector: 'app-staff',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './staff.component.html',
    styleUrl: '../admin/admin.component.scss' // Reusing admin dashboard styles implicitly by copy
})
export class StaffComponent implements OnInit {
    user: any = null;
    activeTab: string = 'students';
    isSidebarExpanded: boolean = false;
    documents: any[] = [];
    notifications: any[] = [];
    students: any[] = [];
    searchQuery: string = '';
    documentSearchQuery: string = '';
    showProfileDetails: boolean = false;

    reviewModalOpen = false;
    selectedDoc: any = null;
    reviewForm = { remark: '', status: 'APPROVED' };
    reviewMessage = '';

    statusFilter: string = 'ALL'; // ALL, PENDING, APPROVED, REJECTED
    selectedStudentView: any = null;

    constructor(private authService: AuthService, private apiService: apiService, private router: Router) { }

    ngOnInit(): void {
        if (!this.authService.getToken()) {
            this.router.navigate(['/login']);
            return;
        }
        this.user = this.authService.getUser();
        if (this.user.role !== 'ROLE_STAFF') {
            this.router.navigate(['/login']);
        }
        this.loadData();
    }

    loadData() {
        this.apiService.getAllDocuments().subscribe(res => {
            this.documents = res;
            if (this.selectedStudentView) {
                this.selectedStudentView = this.groupedStudents.find((g: any) => g.student.id === this.selectedStudentView.student.id) || null;
            }
        });
        this.apiService.getStudents().subscribe(res => this.students = res);
        this.loadNotifications();
    }

    loadNotifications() {
        this.apiService.getNotifications().subscribe(res => {
            this.notifications = res.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        });
    }

    getFilteredDocuments() {
        if (this.statusFilter === 'ALL') {
            return this.documents;
        }
        return this.documents.filter(doc => doc.status === this.statusFilter);
    }

    get groupedStudents() {
        // ... (existing logic)
        const map = new Map<number, any>();
        const filteredDocs = this.getFilteredDocuments();

        for (const doc of filteredDocs) {
            const studentId = doc.student.id;
            if (!map.has(studentId)) {
                map.set(studentId, { student: doc.student, documents: [], hasUpdates: false });
            }
            const group = map.get(studentId);
            group.documents.push(doc);
            if (doc.status === 'PENDING') {
                group.hasUpdates = true;
            }
        }

        let groups = Array.from(map.values());

        if (this.documentSearchQuery) {
            const q = this.documentSearchQuery.toLowerCase();
            groups = groups.filter(g =>
                (g.student.name && g.student.name.toLowerCase().includes(q)) ||
                (g.student.email && g.student.email.toLowerCase().includes(q)) ||
                (g.student.rollNo && g.student.rollNo.toLowerCase().includes(q))
            );
        }

        return groups;
    }

    get filteredStudents() {
        if (!this.searchQuery) return this.students;
        const q = this.searchQuery.toLowerCase();
        return this.students.filter(student =>
            (student.name && student.name.toLowerCase().includes(q)) ||
            (student.email && student.email.toLowerCase().includes(q)) ||
            (student.rollNo && student.rollNo.toLowerCase().includes(q))
        );
    }

    viewStudent(group: any) {
        this.selectedStudentView = group;
    }

    closeStudentView() {
        this.selectedStudentView = null;
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

    openReviewModal(doc: any) {
        this.selectedDoc = doc;
        this.reviewForm = { remark: '', status: 'APPROVED' };
        this.reviewMessage = '';
        this.reviewModalOpen = true;
    }

    closeReviewModal() {
        this.reviewModalOpen = false;
        this.selectedDoc = null;
    }

    submitReview() {
        this.apiService.reviewDocument(this.selectedDoc.id, this.reviewForm.remark, this.reviewForm.status).subscribe({
            next: (res) => {
                this.reviewMessage = res.message;
                setTimeout(() => {
                    this.closeReviewModal();
                    this.loadData();
                }, 1000);
            },
            error: (err) => {
                this.reviewMessage = err.error?.message || 'Error publishing review';
            }
        });
    }

    showRequestModal = false;
    requestForm = { documentName: '', note: '' };
    requestMessage = '';

    openRequestModal(): void {
        this.showRequestModal = true;
        this.requestForm = { documentName: '', note: '' };
        this.requestMessage = '';
    }

    closeRequestModal(): void {
        this.showRequestModal = false;
    }

    closeRequestModalOutside(event: MouseEvent): void {
        if ((event.target as HTMLElement).classList.contains('backdrop-bg')) {
            this.closeRequestModal();
        }
    }

    submitRequestAll(): void {
        if (!this.requestForm.documentName) return;
        this.apiService.requestCustomDocumentAll(this.requestForm.documentName, this.requestForm.note)
            .subscribe({
                next: (res) => {
                    this.requestMessage = res.message;
                    setTimeout(() => this.closeRequestModal(), 2000);
                },
                error: (err) => {
                    this.requestMessage = err.error?.message || 'Error sending request';
                }
            });
    }

    markRead(id: number) {
        this.apiService.markNotificationAsRead(id).subscribe(() => this.loadNotifications());
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
