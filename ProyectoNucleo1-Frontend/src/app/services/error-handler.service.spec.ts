import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandlerService } from './error-handler.service';

describe('ErrorHandlerService', () => {
  let servicio: ErrorHandlerService;

  const FALLBACK_POR_DEFECTO = 'Ocurrió un error inesperado.';

  function http(status: number, error?: unknown): HttpErrorResponse {
    return new HttpErrorResponse({ status, error });
  }

  beforeEach(() => {
    servicio = new ErrorHandlerService();
  });

  describe('con HttpErrorResponse', () => {
    it('status 0 indica que no hay conexión con el servidor', () => {
      expect(servicio.extraerMensaje(http(0))).toContain('No se puede conectar con el servidor');
    });

    it('usa body.mensaje cuando existe', () => {
      expect(servicio.extraerMensaje(http(400, { mensaje: 'Nombre inválido' }))).toBe('Nombre inválido');
    });

    it('usa body.message cuando no hay body.mensaje', () => {
      expect(servicio.extraerMensaje(http(500, { message: 'Fallo interno' }))).toBe('Fallo interno');
    });

    it('usa body.error cuando no hay mensaje ni message', () => {
      expect(servicio.extraerMensaje(http(400, { error: 'Bad Request' }))).toBe('Bad Request');
    });

    it('da prioridad a body.mensaje sobre body.message', () => {
      expect(servicio.extraerMensaje(http(400, { mensaje: 'Primero', message: 'Segundo' }))).toBe('Primero');
    });

    it('ignora un mensaje vacío o de solo espacios y cae al mensaje por código', () => {
      expect(servicio.extraerMensaje(http(400, { mensaje: '   ' })))
        .toBe('Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.');
    });

    it('usa el cuerpo cuando es un texto corto, recortado', () => {
      expect(servicio.extraerMensaje(http(409, '  Correo duplicado  '))).toBe('Correo duplicado');
    });

    it('ignora un cuerpo de texto demasiado largo (300 o más caracteres)', () => {
      expect(servicio.extraerMensaje(http(404, 'x'.repeat(300))))
        .toBe('El recurso solicitado no existe.');
    });

    it('ignora un cuerpo de texto vacío', () => {
      expect(servicio.extraerMensaje(http(403, '   ')))
        .toBe('No tienes permisos para realizar esta acción.');
    });

    it('traduce los códigos HTTP conocidos', () => {
      expect(servicio.extraerMensaje(http(400))).toBe('Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.');
      expect(servicio.extraerMensaje(http(401))).toBe('Usuario o contraseña incorrectos.');
      expect(servicio.extraerMensaje(http(403))).toBe('No tienes permisos para realizar esta acción.');
      expect(servicio.extraerMensaje(http(404))).toBe('El recurso solicitado no existe.');
      expect(servicio.extraerMensaje(http(409))).toBe('La operación entra en conflicto con un registro existente.');
    });

    it('devuelve el fallback por defecto para 500 y para códigos desconocidos', () => {
      expect(servicio.extraerMensaje(http(500))).toBe(FALLBACK_POR_DEFECTO);
      expect(servicio.extraerMensaje(http(503))).toBe(FALLBACK_POR_DEFECTO);
    });

    it('devuelve el fallback personalizado cuando se indica', () => {
      expect(servicio.extraerMensaje(http(500), 'No fue posible guardar.')).toBe('No fue posible guardar.');
    });
  });

  describe('con otros tipos de error', () => {
    it('devuelve el mismo texto cuando el error es un string', () => {
      expect(servicio.extraerMensaje('Algo falló')).toBe('Algo falló');
    });

    it('devuelve error.message para un Error estándar', () => {
      expect(servicio.extraerMensaje(new Error('boom'))).toBe('boom');
    });

    it('devuelve el fallback cuando message no es un string', () => {
      expect(servicio.extraerMensaje({ message: 42 })).toBe(FALLBACK_POR_DEFECTO);
    });

    it('devuelve el fallback para null, undefined y números', () => {
      expect(servicio.extraerMensaje(null)).toBe(FALLBACK_POR_DEFECTO);
      expect(servicio.extraerMensaje(undefined)).toBe(FALLBACK_POR_DEFECTO);
      expect(servicio.extraerMensaje(123)).toBe(FALLBACK_POR_DEFECTO);
    });

    it('respeta el fallback personalizado', () => {
      expect(servicio.extraerMensaje(null, 'Personalizado')).toBe('Personalizado');
    });
  });
});
