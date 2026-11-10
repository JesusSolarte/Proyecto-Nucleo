import { DiaSemana } from './dia-semana';

/**
 * DTO de un bloque de disponibilidad semanal.
 * Las horas viajan en formato ISO local, por ejemplo "08:00:00".
 */
export interface BloqueDisponibilidadDTO {
  id?: number;
  idDocente?: number;
  nombreDocente?: string;
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFin: string;
}
