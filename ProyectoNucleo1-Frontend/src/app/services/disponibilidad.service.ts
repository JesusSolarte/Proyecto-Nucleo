import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE } from '../config';
import { UsuarioDTO } from '../models/usuario-dto';
import { BloqueDisponibilidadDTO } from '../models/bloque-disponibilidad-dto';

/**
 * Consume los endpoints de consulta pública y de gestión de bloques de
 * disponibilidad.
 */
@Injectable({ providedIn: 'root' })
export class DisponibilidadService {

  constructor(private http: HttpClient) {}

  listarDocentes(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(`${API_BASE}/api/disponibilidad/docentes`);
  }

  consultarDocente(idDocente: number): Observable<BloqueDisponibilidadDTO[]> {
    return this.http.get<BloqueDisponibilidadDTO[]>(`${API_BASE}/api/disponibilidad/docente/${idDocente}`);
  }

  listarMisBloques(): Observable<BloqueDisponibilidadDTO[]> {
    return this.http.get<BloqueDisponibilidadDTO[]>(`${API_BASE}/api/bloques`);
  }

  crearBloque(bloque: BloqueDisponibilidadDTO): Observable<BloqueDisponibilidadDTO> {
    return this.http.post<BloqueDisponibilidadDTO>(`${API_BASE}/api/bloques`, bloque);
  }

  actualizarBloque(id: number, bloque: BloqueDisponibilidadDTO): Observable<BloqueDisponibilidadDTO> {
    return this.http.put<BloqueDisponibilidadDTO>(`${API_BASE}/api/bloques/${id}`, bloque);
  }

  eliminarBloque(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/bloques/${id}`);
  }
}
