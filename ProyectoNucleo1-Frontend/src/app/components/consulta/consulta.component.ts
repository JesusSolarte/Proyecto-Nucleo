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

const ALTO_HORA = 44;

@Component({
  selector: 'consulta',
  templateUrl: './consulta.component.html',
  styleUrls: ['./consulta.component.css']
})
export class ConsultaComponent implements OnInit {

  readonly dias = DIAS_SEMANA;

  docentes: UsuarioDTO[] = [];
  busqueda = '';
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

  get docentesFiltrados(): UsuarioDTO[] {
    const texto = this.busqueda.trim().toLowerCase();
    return this.docentes.filter(docente => {
      const nombreCompleto = (docente.nombre + ' ' + docente.apellido).toLowerCase();
      return nombreCompleto.includes(texto);
    });
  }

  get docenteSeleccionado(): UsuarioDTO | undefined {
    return this.docentes.find(docente => docente.id === this.idSeleccionado);
  }

  get calendario(): ColumnaDia[] {
    return DIAS_SEMANA.map(dia => ({
      dia: dia.valor,
      etiqueta: dia.etiqueta,
      bloques: this.bloques.filter(bloque => bloque.diaSemana === dia.valor)
    }));
  }

  get bloquesOrdenados(): BloqueDisponibilidadDTO[] {
    const orden = DIAS_SEMANA.map(dia => dia.valor);
    return [...this.bloques].sort((a, b) => {
      const porDia = orden.indexOf(a.diaSemana) - orden.indexOf(b.diaSemana);
      if (porDia !== 0) {
        return porDia;
      }
      return a.horaInicio.localeCompare(b.horaInicio);
    });
  }

  get horas(): number[] {
    let primera = 6;
    let ultima = 22;
    for (const bloque of this.bloques) {
      primera = Math.min(primera, Math.floor(this.aMinutos(bloque.horaInicio) / 60));
      ultima = Math.max(ultima, Math.ceil(this.aMinutos(bloque.horaFin) / 60));
    }
    const lista: number[] = [];
    for (let hora = primera; hora < ultima; hora++) {
      lista.push(hora);
    }
    return lista;
  }

  get diasConDisponibilidad(): number {
    return this.calendario.filter(columna => columna.bloques.length > 0).length;
  }

  get horasSemanales(): string {
    const minutos = this.bloques.reduce((total, bloque) => {
      return total + this.aMinutos(bloque.horaFin) - this.aMinutos(bloque.horaInicio);
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

  seleccionar(id: number | undefined): void {
    if (id === undefined) {
      return;
    }

    this.idSeleccionado = id;
    this.bloques = [];
    this.error = '';
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

  arriba(bloque: BloqueDisponibilidadDTO): number {
    const minutosDesdeInicio = this.aMinutos(bloque.horaInicio) - this.horas[0] * 60;
    return minutosDesdeInicio / 60 * ALTO_HORA;
  }

  alto(bloque: BloqueDisponibilidadDTO): number {
    const duracion = this.aMinutos(bloque.horaFin) - this.aMinutos(bloque.horaInicio);
    return duracion / 60 * ALTO_HORA;
  }

  horaCorta(hora: string): string {
    return hora.substring(0, 5);
  }

  etiquetaDia(dia: DiaSemana): string {
    const encontrado = DIAS_SEMANA.find(item => item.valor === dia);
    return encontrado ? encontrado.etiqueta : dia;
  }

  private aMinutos(hora: string): number {
    const partes = hora.split(':');
    return Number(partes[0]) * 60 + Number(partes[1]);
  }
}
