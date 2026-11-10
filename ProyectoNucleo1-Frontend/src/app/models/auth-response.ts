import { UsuarioDTO } from './usuario-dto';

/**
 * Respuesta del endpoint de inicio de sesión.
 */
export interface AuthResponse {
  token: string;
  tipoToken: string;
  usuario: UsuarioDTO;
}
