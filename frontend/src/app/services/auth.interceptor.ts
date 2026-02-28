import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor() { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let authReq = req;
        const token = window.localStorage.getItem('auth-token');

        if (token != null) {
            authReq = req.clone({ headers: req.headers.set('Authorization', 'Bearer ' + token) });
        }
        return next.handle(authReq);
    }
}
