import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { DisponibilidadService } from './disponibilidad.service';
import { API_BASE } from '../config';
import { UsuarioDTO } from '../models/usuario-dto';
import { BloqueDisponibilidadDTO } from '../models/bloque-disponibilidad-dto';

describe('DisponibilidadService', () => {
  let service: DisponibilidadService;
  let httpMock: HttpTestingController;

  const bloque: BloqueDisponibilidadDTO = {
    id: 10,
    idDocente: 1,
    nombreDocente: 'Ana García',
    diaSemana: 'LUNES',
    horaInicio: '08:00:00',
    horaFin: '10:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(DisponibilidadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('se crea correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('listarDocentes() hace GET a /api/disponibilidad/docentes', () => {
    const docentes: UsuarioDTO[] = [
      { id: 1, nombre: 'Ana', apellido: 'García', correo: '', nombreUsuario: '' }
    ];
    const resultados: UsuarioDTO[][] = [];

    service.listarDocentes().subscribe(r => resultados.push(r));

    const req = httpMock.expectOne(`${API_BASE}/api/disponibilidad/docentes`);
    expect(req.request.method).toBe('GET');
    req.flush(docentes);
    expect(resultados).toEqual([docentes]);
  });

  it('consultarDocente() hace GET a /api/disponibilidad/docente/:id', () => {
    const resultados: BloqueDisponibilidadDTO[][] = [];

    service.consultarDocente(7).subscribe(r => resultados.push(r));

    const req = httpMock.expectOne(`${API_BASE}/api/disponibilidad/docente/7`);
    expect(req.request.method).toBe('GET');
    req.flush([bloque]);
    expect(resultados).toEqual([[bloque]]);
  });

  it('listarMisBloques() hace GET a /api/bloques', () => {
    const resultados: BloqueDisponibilidadDTO[][] = [];

    service.listarMisBloques().subscribe(r => resultados.push(r));

    const req = httpMock.expectOne(`${API_BASE}/api/bloques`);
    expect(req.request.method).toBe('GET');
    req.flush([bloque]);
    expect(resultados).toEqual([[bloque]]);
  });

  it('crearBloque() hace POST a /api/bloques con el bloque en el cuerpo', () => {
    const nuevo: BloqueDisponibilidadDTO = { diaSemana: 'MARTES', horaInicio: '09:00', horaFin: '11:00' };
    const resultados: BloqueDisponibilidadDTO[] = [];

    service.crearBloque(nuevo).subscribe(r => resultados.push(r));

    const req = httpMock.expectOne(`${API_BASE}/api/bloques`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevo);
    req.flush({ ...nuevo, id: 11 });
    expect(resultados[0].id).toBe(11);
  });

  it('actualizarBloque() hace PUT a /api/bloques/:id con el bloque en el cuerpo', () => {
    const cambios: BloqueDisponibilidadDTO = { diaSemana: 'JUEVES', horaInicio: '14:00', horaFin: '16:00' };

    service.actualizarBloque(10, cambios).subscribe();

    const req = httpMock.expectOne(`${API_BASE}/api/bloques/10`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(cambios);
    req.flush({ ...cambios, id: 10 });
  });

  it('eliminarBloque() hace DELETE a /api/bloques/:id', () => {
    const completado = jasmine.createSpy('completado');

    service.eliminarBloque(10).subscribe({ complete: completado });

    const req = httpMock.expectOne(`${API_BASE}/api/bloques/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
    expect(completado).toHaveBeenCalled();
  });

  it('propaga el error cuando el servidor responde con 403', () => {
    const errorSpy = jasmine.createSpy('error');

    service.eliminarBloque(10).subscribe({ error: errorSpy });
    httpMock
      .expectOne(`${API_BASE}/api/bloques/10`)
      .flush({ mensaje: 'Sin permiso' }, { status: 403, statusText: 'Forbidden' });

    expect(errorSpy).toHaveBeenCalled();
  });
});
