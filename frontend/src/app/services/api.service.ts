import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const API_URL = 'https://institutional-backend.onrender.com/api/';

@Injectable({
    providedIn: 'root'
})
export class apiService {

    constructor(private http: HttpClient) { }

    // Admin
    registerStaff(staff: any): Observable<any> {
        return this.http.post(API_URL + 'admin/register-staff', staff);
    }

    getStaff(): Observable<any> {
        return this.http.get(API_URL + 'admin/staff');
    }

    getStudents(): Observable<any> {
        return this.http.get(API_URL + 'auth/students');
    }

    toggleBlockStaff(id: number): Observable<any> {
        return this.http.put(API_URL + `admin/staff/${id}/toggle-block`, {});
    }

    deleteStaff(id: number): Observable<any> {
        return this.http.delete(API_URL + `admin/staff/${id}`);
    }

    getAuditLogs(): Observable<any> {
        return this.http.get(API_URL + 'audit/all');
    }

    // Documents
    uploadDocument(formData: FormData): Observable<any> {
        return this.http.post(API_URL + 'documents/upload', formData);
    }

    downloadDocument(id: number): Observable<Blob> {
        return this.http.get(API_URL + `documents/download/${id}`, { responseType: 'blob' });
    }

    previewDocument(id: number): Observable<Blob> {
        return this.http.get(API_URL + `documents/preview/${id}`, { responseType: 'blob' });
    }

    deleteDocument(id: number): Observable<any> {
        return this.http.delete(API_URL + `documents/${id}`);
    }

    reviewDocument(id: number, remark: string, statusStr: string): Observable<any> {
        return this.http.post(API_URL + `documents/${id}/review`, null, {
            params: { remark, status: statusStr }
        });
    }

    getStudentDocuments(studentId: number): Observable<any> {
        return this.http.get(API_URL + `documents/student/${studentId}`);
    }

    getAllDocuments(): Observable<any> {
        return this.http.get(API_URL + 'documents/all');
    }

    requestCustomDocumentAll(documentName: string, note: string): Observable<any> {
        return this.http.post(API_URL + 'documents/request-all', { documentName, note });
    }

    // Notifications
    getNotifications(): Observable<any> {
        return this.http.get(API_URL + 'notifications/all');
    }

    getUnreadNotifications(): Observable<any> {
        return this.http.get(API_URL + 'notifications/unread');
    }

    markNotificationAsRead(id: number): Observable<any> {
        return this.http.put(API_URL + `notifications/${id}/read`, {});
    }

    // Profile
    updateProfile(data: any): Observable<any> {
        return this.http.put(API_URL + 'auth/student/update-profile', data);
    }

    updateStaffInfo(id: number, name: string, email: string): Observable<any> {
        return this.http.put(API_URL + `admin/staff/${id}/info`, { name, email });
    }
}
