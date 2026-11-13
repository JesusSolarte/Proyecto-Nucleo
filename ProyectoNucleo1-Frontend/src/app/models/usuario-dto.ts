export interface UsuarioDTO {
  id?: number;
  nombre: string;
  apellido: string;
  correo: string;
  nombreUsuario: string;
  contrasena?: string;
  telefono?: string;
  rol?: 'DOCENTE' | 'ADMIN';
}
