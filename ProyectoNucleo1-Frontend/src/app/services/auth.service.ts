import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { finalize, tap } from 'rxjs/operators';

import { API_BASE } from '../config';
import { UsuarioDTO } from '../models/usuario-dto';
import { AuthResponse } from '../models/auth-response';

const CLAVE_TOKEN = 'jwt_token';
const CLAVE_USUARIO = 'usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private userSubject = new BehaviorSubject<UsuarioDTO | null>(this.cargarUsuario());
  currentUser$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(nombreUsuario: string, contrasena: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_BASE}/api/auth/login`, { nombreUsuario, contrasena }).pipe(
      tap(respuesta => this.persistirSesion(respuesta))
    );
  }

  registrar(usuario: UsuarioDTO): Observable<UsuarioDTO> {
    return this.http.post<UsuarioDTO>(`${API_BASE}/api/auth/registro`, usuario);
  }

  logout(): Observable<unknown> {
    return this.http.post(`${API_BASE}/api/auth/logout`, {}).pipe(
      finalize(() => this.limpiarSesion())
    );
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(CLAVE_TOKEN) && !!this.userSubject.value;
  }

  isAdmin(): boolean {
    return this.userSubject.value?.rol === 'ADMIN';
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.userSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem(CLAVE_TOKEN);
  }

  clearLocal(): void {
    this.limpiarSesion();
  }

  private persistirSesion(respuesta: AuthResponse): void {
    localStorage.setItem(CLAVE_TOKEN, respuesta.token);
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify(respuesta.usuario));
    this.userSubject.next(respuesta.usuario);
  }

  private limpiarSesion(): void {
    localStorage.removeItem(CLAVE_TOKEN);
    localStorage.removeItem(CLAVE_USUARIO);
    this.userSubject.next(null);
  }

  private cargarUsuario(): UsuarioDTO | null {
    try {
      const crudo = localStorage.getItem(CLAVE_USUARIO);
      return crudo ? (JSON.parse(crudo) as UsuarioDTO) : null;
    } catch {
      return null;
    }
  }
}
