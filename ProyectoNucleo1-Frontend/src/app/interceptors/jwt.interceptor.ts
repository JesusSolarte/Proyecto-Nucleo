import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
      return next.handle(req);
    }

    if (this.tokenVencido(token)) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('usuario');
      window.location.href = '/login';
      return next.handle(req);
    }

    return next.handle(req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    }));
  }

  private tokenVencido(token: string): boolean {
    try {
      const datos = JSON.parse(atob(token.split('.')[1]));
      return datos.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
