import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import Swal from 'sweetalert2';

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

const ALTO_HORA = 44;

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  readonly dias = DIAS_SEMANA;

  bloques: BloqueDisponibilidadDTO[] = [];
  cargando = true;
  guardando = false;
  error = '';
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
      Swal.fire('Faltan datos', 'Selecciona el día, la hora de inicio y la hora de fin.', 'warning');
      return;
    }

    const { diaSemana, horaInicio, horaFin } = this.form.value;

    if (horaInicio >= horaFin) {
      Swal.fire('Rango inválido', 'La hora de inicio debe ser anterior a la hora de fin.', 'warning');
      return;
    }

    const payload: BloqueDisponibilidadDTO = {
      diaSemana,
      horaInicio,
      horaFin
    };

    const estabaEditando = this.editandoId !== null;
    this.guardando = true;

    const peticion = this.editandoId !== null
      ? this.disponibilidad.actualizarBloque(this.editandoId, payload)
      : this.disponibilidad.crearBloque(payload);

    peticion.subscribe({
      next: () => {
        this.guardando = false;
        Swal.fire({
          icon: 'success',
          title: estabaEditando ? 'Bloque actualizado' : 'Bloque registrado',
          timer: 1600,
          showConfirmButton: false
        });
        this.cancelarEdicion();
        this.cargarBloques();
      },
      error: (err) => {
        this.guardando = false;
        Swal.fire('No se guardó el bloque', this.errorHandler.extraerMensaje(err, 'No fue posible guardar el bloque.'), 'error');
      }
    });
  }

  eliminar(bloque: BloqueDisponibilidadDTO): void {
    if (bloque.id === undefined) {
      return;
    }
    const id = bloque.id;

    Swal.fire({
      icon: 'question',
      title: '¿Eliminar este bloque?',
      text: `${this.etiquetaDia(bloque.diaSemana)} de ${this.horaCorta(bloque.horaInicio)} a ${this.horaCorta(bloque.horaFin)}`,
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#c0392b'
    }).then(resultado => {
      if (!resultado.isConfirmed) {
        return;
      }

      this.disponibilidad.eliminarBloque(id).subscribe({
        next: () => {
          Swal.fire({ icon: 'success', title: 'Bloque eliminado', timer: 1600, showConfirmButton: false });
          if (this.editandoId === id) {
            this.cancelarEdicion();
          }
          this.cargarBloques();
        },
        error: (err) => {
          Swal.fire('No se eliminó el bloque', this.errorHandler.extraerMensaje(err, 'No fue posible eliminar el bloque.'), 'error');
        }
      });
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
