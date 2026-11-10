import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { DisponibilidadService } from '../../services/disponibilidad.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';
import { BloqueDisponibilidadDTO } from '../../models/bloque-disponibilidad-dto';
import { DiaSemana, DIAS_SEMANA } from '../../models/dia-semana';

interface ColumnaDia {
  dia: DiaSemana;
  etiqueta: string;
  bloques: BloqueDisponibilidadDTO[];
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  readonly dias = DIAS_SEMANA;

  bloques: BloqueDisponibilidadDTO[] = [];
  cargando = true;
  guardando = false;
  error = '';
  exito = '';
  editandoId: number | null = null;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private disponibilidad: DisponibilidadService,
    public auth: AuthService,
    private errorHandler: ErrorHandlerService
  ) {
    this.form = this.fb.group({
      diaSemana: ['LUNES', Validators.required],
      horaInicio: ['08:00', Validators.required],
      horaFin: ['10:00', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarBloques();
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

  get bloquesOrdenados(): BloqueDisponibilidadDTO[] {
    const orden = new Map(DIAS_SEMANA.map((dia, indice) => [dia.valor, indice]));
    return [...this.bloques].sort((a, b) => {
      const porDia = (orden.get(a.diaSemana) ?? 0) - (orden.get(b.diaSemana) ?? 0);
      return porDia !== 0 ? porDia : a.horaInicio.localeCompare(b.horaInicio);
    });
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

  cargarBloques(): void {
    this.cargando = true;
    this.error = '';

    this.disponibilidad.listarMisBloques().subscribe({
      next: (bloques) => {
        this.bloques = bloques;
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible cargar tus bloques.');
      }
    });
  }

  editar(bloque: BloqueDisponibilidadDTO): void {
    if (bloque.id === undefined) {
      return;
    }
    this.editandoId = bloque.id;
    this.exito = '';
    this.error = '';
    this.form.patchValue({
      diaSemana: bloque.diaSemana,
      horaInicio: this.horaCorta(bloque.horaInicio),
      horaFin: this.horaCorta(bloque.horaFin)
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.form.reset({ diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { diaSemana, horaInicio, horaFin } = this.form.value;

    if (horaInicio >= horaFin) {
      this.error = 'La hora de inicio debe ser anterior a la hora de fin.';
      return;
    }

    const payload: BloqueDisponibilidadDTO = {
      diaSemana,
      horaInicio,
      horaFin
    };

    this.guardando = true;
    this.error = '';
    this.exito = '';

    const peticion = this.editandoId !== null
      ? this.disponibilidad.actualizarBloque(this.editandoId, payload)
      : this.disponibilidad.crearBloque(payload);

    peticion.subscribe({
      next: () => {
        this.guardando = false;
        this.exito = this.editandoId !== null
          ? 'Bloque actualizado correctamente.'
          : 'Bloque registrado correctamente.';
        this.cancelarEdicion();
        this.cargarBloques();
      },
      error: (err) => {
        this.guardando = false;
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible guardar el bloque.');
      }
    });
  }

  eliminar(bloque: BloqueDisponibilidadDTO): void {
    if (bloque.id === undefined) {
      return;
    }
    const confirmado = window.confirm(
      `¿Eliminar el bloque del ${this.etiquetaDia(bloque.diaSemana)} de ${this.horaCorta(bloque.horaInicio)} a ${this.horaCorta(bloque.horaFin)}?`
    );
    if (!confirmado) {
      return;
    }

    this.error = '';
    this.exito = '';

    this.disponibilidad.eliminarBloque(bloque.id).subscribe({
      next: () => {
        this.exito = 'Bloque eliminado correctamente.';
        if (this.editandoId === bloque.id) {
          this.cancelarEdicion();
        }
        this.cargarBloques();
      },
      error: (err) => {
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible eliminar el bloque.');
      }
    });
  }

  horaCorta(hora: string): string {
    return hora.substring(0, 5);
  }

  etiquetaDia(dia: DiaSemana): string {
    const encontrado = DIAS_SEMANA.find(item => item.valor === dia);
    return encontrado ? encontrado.etiqueta : dia;
  }

  private diferenciaMinutos(inicio: string, fin: string): number {
    return this.aMinutos(fin) - this.aMinutos(inicio);
  }

  private aMinutos(hora: string): number {
    const [horas, minutos] = hora.split(':').map(Number);
    return horas * 60 + minutos;
  }
}
