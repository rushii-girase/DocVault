import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const AUTH_API = 'http://localhost:8080/api/auth/';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(private http: HttpClient) { }

    login(credentials: any): Observable<any> {
        return this.http.post(AUTH_API + 'login', {
            email: credentials.email,
            password: credentials.password
        });
    }

    registerStudent(user: any): Observable<any> {
        return this.http.post(AUTH_API + 'register-student', user);
    }

    verifyEmail(token: string): Observable<any> {
        return this.http.get(AUTH_API + 'verify-email?token=' + token);
    }

    saveToken(token: string): void {
        window.localStorage.removeItem('auth-token');
        window.localStorage.setItem('auth-token', token);
    }

    getToken(): string | null {
        return window.localStorage.getItem('auth-token');
    }

    saveUser(user: any): void {
        window.localStorage.removeItem('auth-user');
        window.localStorage.setItem('auth-user', JSON.stringify(user));
    }

    getUser(): any {
        const user = window.localStorage.getItem('auth-user');
        if (user) {
            return JSON.parse(user);
        }
        return {};
    }

    logout(): void {
        window.localStorage.clear();
    }
}
