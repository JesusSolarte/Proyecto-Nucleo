import { TestBed } from '@angular/core/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { JwtInterceptor } from './jwt.interceptor';

/** Construye un JWT de mentira cuyo payload contiene la expiración indicada (en segundos). */
function crearToken(expEnSegundos: number): string {
  const encabezado = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const cuerpo = btoa(JSON.stringify({ sub: 'ana.garcia', exp: expEnSegundos }));
  return `${encabezado}.${cuerpo}.firma`;
}

/**
 * Nota: la rama "token vencido / malformado" asigna `window.location.href = '/login'`,
 * lo que recargaría la página de Karma y abortaría la ejecución de las pruebas. Por eso
 * aquí solo se prueban las ramas que no navegan.
 */
describe('JwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('no agrega el encabezado Authorization cuando no hay token', () => {
    http.get('/api/prueba').subscribe();

    const req = httpMock.expectOne('/api/prueba');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('agrega "Authorization: Bearer <token>" cuando el token está vigente', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) + 3600);
    localStorage.setItem('jwt_token', token);

    http.get('/api/prueba').subscribe();

    const req = httpMock.expectOne('/api/prueba');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush({});
  });

  it('mantiene método, URL y cuerpo de la petición original', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) + 3600);
    localStorage.setItem('jwt_token', token);

    http.post('/api/bloques', { dia: 'LUNES' }).subscribe();

    const req = httpMock.expectOne('/api/bloques');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ dia: 'LUNES' });
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush({});
  });

  it('no modifica el token guardado en localStorage', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) + 3600);
    localStorage.setItem('jwt_token', token);

    http.get('/api/prueba').subscribe();
    httpMock.expectOne('/api/prueba').flush({});

    expect(localStorage.getItem('jwt_token')).toBe(token);
  });
});
