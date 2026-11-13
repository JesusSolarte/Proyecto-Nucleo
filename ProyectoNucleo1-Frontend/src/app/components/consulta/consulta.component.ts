import { Component, OnInit } from '@angular/core';

import { DisponibilidadService } from '../../services/disponibilidad.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { UsuarioDTO } from '../../models/usuario-dto';
import { BloqueDisponibilidadDTO } from '../../models/bloque-disponibilidad-dto';
import { DiaSemana, DIAS_SEMANA } from '../../models/dia-semana';

interface ColumnaDia {
  dia: DiaSemana;
  etiqueta: string;
  bloques: BloqueDisponibilidadDTO[];
}

@Component({
  selector: 'consulta',
  templateUrl: './consulta.component.html',
  styleUrls: ['./consulta.component.css']
})
export class ConsultaComponent implements OnInit {

  readonly dias = DIAS_SEMANA;

  docentes: UsuarioDTO[] = [];
  idSeleccionado: number | null = null;
  bloques: BloqueDisponibilidadDTO[] = [];

  cargandoDocentes = true;
  cargandoBloques = false;
  error = '';

  constructor(
    private disponibilidad: DisponibilidadService,
    private errorHandler: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.cargarDocentes();
  }

  get docenteSeleccionado(): UsuarioDTO | undefined {
    return this.docentes.find(docente => docente.id === this.idSeleccionado);
  }

  get calendario(): ColumnaDia[] {
    return DIAS_SEMANA.map(dia => ({
      dia: dia.valor,
      etiqueta: dia.etiqueta,
      bloques: this.bloques
        .filter(bloque => bloque.diaSemana === dia.valor)
        .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio))
    }));
  }

  get diasConDisponibilidad(): number {
    return this.calendario.filter(columna => columna.bloques.length > 0).length;
  }

  get horasSemanales(): string {
    const minutos = this.bloques.reduce((total, bloque) => {
      return total + this.diferenciaMinutos(bloque.horaInicio, bloque.horaFin);
    }, 0);
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
  }

  cargarDocentes(): void {
    this.cargandoDocentes = true;
    this.error = '';

    this.disponibilidad.listarDocentes().subscribe({
      next: (docentes) => {
        this.docentes = docentes;
        this.cargandoDocentes = false;
      },
      error: (err) => {
        this.cargandoDocentes = false;
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible cargar los docentes.');
      }
    });
  }

  seleccionar(id: number | null): void {
    this.idSeleccionado = id;
    this.bloques = [];
    this.error = '';

    if (id === null || id === undefined) {
      return;
    }

    this.cargandoBloques = true;

    this.disponibilidad.consultarDocente(id).subscribe({
      next: (bloques) => {
        this.bloques = bloques;
        this.cargandoBloques = false;
      },
      error: (err) => {
        this.cargandoBloques = false;
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible consultar la disponibilidad.');
      }
    });
  }

  horaCorta(hora: string): string {
    return hora.substring(0, 5);
  }

  private diferenciaMinutos(inicio: string, fin: string): number {
    return this.aMinutos(fin) - this.aMinutos(inicio);
  }

  private aMinutos(hora: string): number {
    const [horas, minutos] = hora.split(':').map(Number);
    return horas * 60 + minutos;
  }
}
