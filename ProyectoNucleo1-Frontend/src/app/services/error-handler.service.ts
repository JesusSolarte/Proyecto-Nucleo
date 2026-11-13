import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {

  extraerMensaje(err: unknown, fallback: string = 'Ocurrió un error inesperado.'): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 0) {
        return 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose.';
      }

      const body = err.error;

      if (body && typeof body === 'object') {
        const mensaje = (body as { mensaje?: string; message?: string; error?: string }).mensaje
          ?? (body as { message?: string }).message
          ?? (body as { error?: string }).error;
        if (typeof mensaje === 'string' && mensaje.trim().length > 0) {
          return mensaje;
        }
      }

      if (typeof body === 'string' && body.trim().length > 0 && body.trim().length < 300) {
        return body.trim();
      }

      return this.mensajePorCodigo(err.status, fallback);
    }

    if (typeof err === 'string') {
      return err;
    }

    if (err && typeof err === 'object' && 'message' in err) {
      const mensaje = (err as { message?: unknown }).message;
      if (typeof mensaje === 'string') {
        return mensaje;
      }
    }

    return fallback;
  }

  private mensajePorCodigo(status: number, fallback: string): string {
    switch (status) {
      case 400: return 'Los datos enviados no son válidos. Revisa los campos e intenta de nuevo.';
      case 401: return 'Usuario o contraseña incorrectos.';
      case 403: return 'No tienes permisos para realizar esta acción.';
      case 404: return 'El recurso solicitado no existe.';
      case 409: return 'La operación entra en conflicto con un registro existente.';
      case 500: return fallback;
      default: return fallback;
    }
  }
}
