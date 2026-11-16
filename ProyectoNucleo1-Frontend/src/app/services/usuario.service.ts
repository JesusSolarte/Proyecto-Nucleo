import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE } from '../config';
import { UsuarioDTO } from '../models/usuario-dto';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

    constructor(private http: HttpClient) {}

    listar(): Observable<UsuarioDTO[]> {
        return this.http.get<UsuarioDTO[]>(`${API_BASE}/api/usuarios`);
    }

    obtenerPorId(id: number): Observable<UsuarioDTO> {
        return this.http.get<UsuarioDTO>(`${API_BASE}/api/usuarios/${id}`);
    }

    crear(usuario: UsuarioDTO): Observable<UsuarioDTO> {
        return this.http.post<UsuarioDTO>(`${API_BASE}/api/usuarios`, usuario);
    }

    actualizar(id: number, usuario: UsuarioDTO): Observable<UsuarioDTO> {
        return this.http.put<UsuarioDTO>(`${API_BASE}/api/usuarios/${id}`, usuario);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${API_BASE}/api/usuarios/${id}`);
    }
}