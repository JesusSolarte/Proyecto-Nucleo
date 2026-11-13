import { DiaSemana } from './dia-semana';

export interface BloqueDisponibilidadDTO {
  id?: number;
  idDocente?: number;
  nombreDocente?: string;
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFin: string;
}
