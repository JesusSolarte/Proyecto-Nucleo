import { UsuarioDTO } from './usuario-dto';

export interface AuthResponse {
  token: string;
  tipoToken: string;
  usuario: UsuarioDTO;
}
