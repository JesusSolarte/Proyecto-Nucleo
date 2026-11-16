import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { API_BASE } from '../config';
import { AuthResponse } from '../models/auth-response';
import { UsuarioDTO } from '../models/usuario-dto';

/** Construye un JWT de mentira cuyo payload contiene la expiración indicada (en segundos). */
function crearToken(expEnSegundos: number): string {
  const encabezado = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const cuerpo = btoa(JSON.stringify({ sub: 'ana.garcia', exp: expEnSegundos }));
  return `${encabezado}.${cuerpo}.firma`;
}

describe('AuthService', () => {
  let httpMock: HttpTestingController;

  const usuario: UsuarioDTO = {
    id: 1,
    nombre: 'Ana',
    apellido: 'García',
    correo: 'ana@correo.com',
    nombreUsuario: 'ana.garcia',
    rol: 'DOCENTE'
  };
  const admin: UsuarioDTO = { ...usuario, id: 2, nombreUsuario: 'admin', rol: 'ADMIN' };

  const futuro = (): number => Math.floor(Date.now() / 1000) + 3600;
  const pasado = (): number => Math.floor(Date.now() / 1000) - 3600;

  /** Deja una sesión guardada en localStorage ANTES de crear el servicio. */
  function sembrarSesion(token: string, user: UsuarioDTO = usuario): void {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('usuario', JSON.stringify(user));
  }

  /** El servicio lee localStorage en su construcción, por eso se crea de forma diferida. */
  function crearServicio(): AuthService {
    return TestBed.inject(AuthService);
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('se crea correctamente', () => {
    expect(crearServicio()).toBeTruthy();
  });

  describe('login()', () => {
    it('envía POST con las credenciales y persiste token y usuario', () => {
      const service = crearServicio();
      const token = crearToken(futuro());
      const respuesta: AuthResponse = { token, tipoToken: 'Bearer', usuario };
      const resultados: AuthResponse[] = [];
      const emitidos: Array<UsuarioDTO | null> = [];
      service.currentUser$.subscribe(u => emitidos.push(u));

      service.login('ana.garcia', 'Clave1234').subscribe(r => resultados.push(r));

      const req = httpMock.expectOne(`${API_BASE}/api/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ nombreUsuario: 'ana.garcia', contrasena: 'Clave1234' });
      req.flush(respuesta);

      expect(resultados).toEqual([respuesta]);
      expect(localStorage.getItem('jwt_token')).toBe(token);
      expect(JSON.parse(localStorage.getItem('usuario') as string)).toEqual(usuario);
      expect(emitidos[emitidos.length - 1]).toEqual(usuario);
    });

    it('con error del servidor no persiste la sesión', () => {
      const service = crearServicio();
      const errorSpy = jasmine.createSpy('error');

      service.login('ana.garcia', 'incorrecta').subscribe({ error: errorSpy });
      httpMock
        .expectOne(`${API_BASE}/api/auth/login`)
        .flush({ mensaje: 'Credenciales inválidas' }, { status: 401, statusText: 'Unauthorized' });

      expect(errorSpy).toHaveBeenCalled();
      expect(localStorage.getItem('jwt_token')).toBeNull();
      expect(service.getCurrentUser()).toBeNull();
    });
  });

  describe('registrar()', () => {
    it('envía POST con el usuario y NO inicia sesión', () => {
      const service = crearServicio();
      const nuevo: UsuarioDTO = {
        nombre: 'Ana',
        apellido: 'García',
        correo: 'ana@correo.com',
        nombreUsuario: 'ana.garcia',
        contrasena: 'Clave1234',
        telefono: '3001234567'
      };
      const resultados: UsuarioDTO[] = [];

      service.registrar(nuevo).subscribe(r => resultados.push(r));

      const req = httpMock.expectOne(`${API_BASE}/api/auth/registro`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(nuevo);
      req.flush(usuario);

      expect(resultados).toEqual([usuario]);
      expect(localStorage.getItem('jwt_token')).toBeNull();
    });
  });

  describe('logout()', () => {
    it('envía POST y limpia la sesión al finalizar', () => {
      sembrarSesion(crearToken(futuro()));
      const service = crearServicio();

      service.logout().subscribe();

      const req = httpMock.expectOne(`${API_BASE}/api/auth/logout`);
      expect(req.request.method).toBe('POST');
      expect(localStorage.getItem('jwt_token')).not.toBeNull();

      req.flush({ mensaje: 'Sesión cerrada correctamente.' });

      expect(localStorage.getItem('jwt_token')).toBeNull();
      expect(localStorage.getItem('usuario')).toBeNull();
      expect(service.getCurrentUser()).toBeNull();
    });

    it('limpia la sesión local incluso si el servidor responde con error', () => {
      sembrarSesion(crearToken(futuro()));
      const service = crearServicio();
      const errorSpy = jasmine.createSpy('error');

      service.logout().subscribe({ error: errorSpy });
      httpMock
        .expectOne(`${API_BASE}/api/auth/logout`)
        .flush('fallo', { status: 500, statusText: 'Server Error' });

      expect(errorSpy).toHaveBeenCalled();
      expect(localStorage.getItem('jwt_token')).toBeNull();
      expect(service.getCurrentUser()).toBeNull();
    });
  });

  describe('isLoggedIn()', () => {
    it('devuelve false cuando no hay token', () => {
      expect(crearServicio().isLoggedIn()).toBeFalse();
    });

    it('devuelve false cuando hay token pero no hay usuario', () => {
      localStorage.setItem('jwt_token', crearToken(futuro()));
      expect(crearServicio().isLoggedIn()).toBeFalse();
    });

    it('devuelve true con token vigente y usuario guardado', () => {
      sembrarSesion(crearToken(futuro()));
      expect(crearServicio().isLoggedIn()).toBeTrue();
    });

    it('devuelve false y limpia la sesión si el token está vencido', () => {
      sembrarSesion(crearToken(pasado()));
      const service = crearServicio();

      expect(service.isLoggedIn()).toBeFalse();
      expect(localStorage.getItem('jwt_token')).toBeNull();
      expect(service.getCurrentUser()).toBeNull();
    });

    it('devuelve false si el token está malformado', () => {
      sembrarSesion('esto-no-es-un-jwt');
      expect(crearServicio().isLoggedIn()).toBeFalse();
    });
  });

  describe('tokenVencido()', () => {
    it('es true para un token con exp en el pasado', () => {
      expect(crearServicio().tokenVencido(crearToken(pasado()))).toBeTrue();
    });

    it('es false para un token con exp en el futuro', () => {
      expect(crearServicio().tokenVencido(crearToken(futuro()))).toBeFalse();
    });

    it('es true para un token malformado', () => {
      expect(crearServicio().tokenVencido('abc')).toBeTrue();
      expect(crearServicio().tokenVencido('a.b.c')).toBeTrue();
    });
  });

  describe('isAdmin()', () => {
    it('es true cuando el usuario guardado tiene rol ADMIN', () => {
      sembrarSesion(crearToken(futuro()), admin);
      expect(crearServicio().isAdmin()).toBeTrue();
    });

    it('es false cuando el usuario es DOCENTE', () => {
      sembrarSesion(crearToken(futuro()), usuario);
      expect(crearServicio().isAdmin()).toBeFalse();
    });

    it('es false cuando no hay usuario', () => {
      expect(crearServicio().isAdmin()).toBeFalse();
    });
  });

  describe('getters y limpieza', () => {
    it('getToken() devuelve el token guardado o null', () => {
      expect(crearServicio().getToken()).toBeNull();
      const token = crearToken(futuro());
      localStorage.setItem('jwt_token', token);
      expect(crearServicio().getToken()).toBe(token);
    });

    it('getCurrentUser() devuelve el usuario cargado desde localStorage', () => {
      sembrarSesion(crearToken(futuro()));
      expect(crearServicio().getCurrentUser()).toEqual(usuario);
    });

    it('currentUser$ emite el usuario inicial almacenado', () => {
      sembrarSesion(crearToken(futuro()));
      const emitidos: Array<UsuarioDTO | null> = [];
      crearServicio().currentUser$.subscribe(u => emitidos.push(u));
      expect(emitidos).toEqual([usuario]);
    });

    it('ignora un usuario corrupto en localStorage', () => {
      localStorage.setItem('usuario', '{esto no es json');
      expect(crearServicio().getCurrentUser()).toBeNull();
    });

    it('clearLocal() borra token y usuario y emite null', () => {
      sembrarSesion(crearToken(futuro()));
      const service = crearServicio();
      const emitidos: Array<UsuarioDTO | null> = [];
      service.currentUser$.subscribe(u => emitidos.push(u));

      service.clearLocal();

      expect(localStorage.getItem('jwt_token')).toBeNull();
      expect(localStorage.getItem('usuario')).toBeNull();
      expect(emitidos[emitidos.length - 1]).toBeNull();
    });
  });
});
