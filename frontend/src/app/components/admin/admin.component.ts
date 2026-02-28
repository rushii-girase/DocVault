import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { apiService } from '../../services/api.service';

@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin.component.html',
    styleUrl: './admin.component.scss'
})
export class AdminComponent implements OnInit {
    user: any;
    activeTab = 'staff';
    auditLogs: any[] = [];
    documents: any[] = [];
    staffMembers: any[] = [];
    students: any[] = [];

    selectedStudentView: any = null;
    searchQuery: string = '';
    documentSearchQuery: string = '';

    staffForm: any = {
        name: '',
        email: '',
        password: '',
        collegeName: '',
        course: ''
    };
    staffMessage = '';

    colleges = [
        'KCTs Late G.N Sapkal College Of Engineering',
        'KCTs R. G. Sapkal College Of Pharmacy',
        'KCTs R. G. Sapkal Intitution Of Pharmacy',
        'KCTs K. R. Sapkal College Of Management Studies'
    ];
    availableCourses: string[] = [];

    constructor(private authService: AuthService, private apiService: apiService, private router: Router) { }

    ngOnInit(): void {
        if (!this.authService.getToken()) {
            this.router.navigate(['/login']);
            return;
        }
        this.user = this.authService.getUser();
        if (this.user.role !== 'ROLE_ADMIN') {
            this.router.navigate(['/login']);
        }
        this.loadData();
    }

    loadData() {
        this.apiService.getAuditLogs().subscribe(res => this.auditLogs = res);
        this.apiService.getStaff().subscribe(res => this.staffMembers = res);
        this.apiService.getStudents().subscribe(res => this.students = res);
        this.apiService.getAllDocuments().subscribe({
            next: (res) => {
                this.documents = res;
                if (this.selectedStudentView) {
                    this.selectedStudentView = this.groupedStudents.find((g: any) => g.student.id === this.selectedStudentView.student.id) || null;
                }
            },
            error: (err) => {
                console.error("Error loading documents:", err);
            }
        });
    }

    get groupedStudents() {
        const map = new Map<number, any>();
        for (const doc of this.documents) {
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

    onCollegeChange() {
        this.staffForm.course = '';
        this.availableCourses = [];

        switch (this.staffForm.collegeName) {
            case 'KCTs Late G.N Sapkal College Of Engineering':
                this.availableCourses = [
                    'E&TC ENGINEERING', 'COMPUTER ENGINEERING', 'AI&DS ENGINEERING',
                    'CIVIL ENGINEERING', 'MECHANICAL ENGINEERING', 'ELECTRICAL ENGINEERING'
                ];
                break;
            case 'KCTs R. G. Sapkal College Of Pharmacy':
                this.availableCourses = ['PHARMACY'];
                this.staffForm.course = 'PHARMACY';
                break;
            case 'KCTs R. G. Sapkal Intitution Of Pharmacy':
                this.availableCourses = ['DIPLOMA', 'PHARMACY'];
                break;
            case 'KCTs K. R. Sapkal College Of Management Studies':
                this.availableCourses = ['MANAGEMENT OF BUSINESS ANALYTICS'];
                this.staffForm.course = 'MANAGEMENT OF BUSINESS ANALYTICS';
                break;
        }
    }

    registerStaff() {
        this.apiService.registerStaff(this.staffForm).subscribe({
            next: (res) => {
                this.staffMessage = res.message;
                this.staffForm = { name: '', email: '', password: '', collegeName: '', course: '' };
                this.loadData();
            },
            error: (err) => {
                this.staffMessage = err.error?.message || 'Error adding staff';
            }
        });
    }

    toggleBlockStaff(id: number) {
        this.apiService.toggleBlockStaff(id).subscribe(() => this.loadData());
    }

    deleteStaff(id: number) {
        if (confirm("Are you sure you want to delete this staff member? This cannot be undone.")) {
            this.apiService.deleteStaff(id).subscribe(() => this.loadData());
        }
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

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
