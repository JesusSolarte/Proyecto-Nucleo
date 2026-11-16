import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

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
    selector: 'admin-bloques',
    templateUrl: './admin-bloques.component.html',
    styleUrls: ['./admin-bloques.component.css']
})
export class AdminBloquesComponent implements OnInit {

    readonly dias = DIAS_SEMANA;

    docentes: UsuarioDTO[] = [];
    bloques: BloqueDisponibilidadDTO[] = [];
    visibles: BloqueDisponibilidadDTO[] = [];
    calendario: ColumnaDia[] = [];
    horas: number[] = [];

    /** Id del docente elegido en el filtro; null = todos. */
    filtroDocente: number | null = null;

    cargando = true;
    guardando = false;
    error = '';
    editandoId: number | null = null;

    form: FormGroup;

    constructor(
        private fb: FormBuilder,
        private disponibilidad: DisponibilidadService,
        private errorHandler: ErrorHandlerService
    ) {
        this.form = this.fb.group({
            idDocente: [null, Validators.required],
            diaSemana: ['LUNES', Validators.required],
            horaInicio: ['08:00', Validators.required],
            horaFin: ['10:00', Validators.required]
        });
    }

    ngOnInit(): void {
        this.cargarTodo();
    }

    get docentesConDisponibilidad(): number {
        return new Set(this.bloques.map(b => b.idDocente)).size;
    }

    get horasSemanales(): string {
        const minutos = this.bloques.reduce((total, bloque) => {
            return total + this.aMinutos(bloque.horaFin) - this.aMinutos(bloque.horaInicio);
        }, 0);
        const horas = Math.floor(minutos / 60);
        const resto = minutos % 60;
        return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
    }

    get docenteFiltrado(): UsuarioDTO | undefined {
        return this.docentes.find(d => d.id === this.filtroDocente);
    }

    cargarTodo(): void {
        this.cargando = true;
        this.error = '';

        forkJoin({
            docentes: this.disponibilidad.listarDocentes(),
            bloques: this.disponibilidad.listarTodosLosBloques()
        }).subscribe({
            next: ({ docentes, bloques }) => {
                this.docentes = docentes;
                this.bloques = bloques;
                if (this.filtroDocente !== null && !docentes.some(d => d.id === this.filtroDocente)) {
                    this.filtroDocente = null;
                }
                this.recalcular();
                this.cargando = false;
            },
            error: (err) => {
                this.cargando = false;
                this.error = this.errorHandler.extraerMensaje(err, 'No fue posible cargar la disponibilidad.');
            }
        });
    }

    cambiarFiltro(valor: number | null): void {
        this.filtroDocente = valor;
        if (this.editandoId === null && valor !== null) {
            this.form.patchValue({ idDocente: valor });
        }
        this.recalcular();
    }

    editar(bloque: BloqueDisponibilidadDTO): void {
        if (bloque.id === undefined) {
            return;
        }
        this.editandoId = bloque.id;
        this.form.patchValue({
            idDocente: bloque.idDocente,
            diaSemana: bloque.diaSemana,
            horaInicio: this.horaCorta(bloque.horaInicio),
            horaFin: this.horaCorta(bloque.horaFin)
        });
        // El backend no permite reasignar un bloque a otro docente.
        this.form.get('idDocente')?.disable();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    cancelarEdicion(): void {
        this.editandoId = null;
        this.form.get('idDocente')?.enable();
        this.form.reset({
            idDocente: this.filtroDocente,
            diaSemana: 'LUNES',
            horaInicio: '08:00',
            horaFin: '10:00'
        });
    }

    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            Swal.fire('Faltan datos', 'Selecciona el docente, el día, la hora de inicio y la hora de fin.', 'warning');
            return;
        }

        const { idDocente, diaSemana, horaInicio, horaFin } = this.form.getRawValue();

        if (horaInicio >= horaFin) {
            Swal.fire('Rango inválido', 'La hora de inicio debe ser anterior a la hora de fin.', 'warning');
            return;
        }

        const estabaEditando = this.editandoId !== null;
        this.guardando = true;

        const peticion = this.editandoId !== null
            ? this.disponibilidad.actualizarBloque(this.editandoId, { diaSemana, horaInicio, horaFin })
            : this.disponibilidad.crearBloque({ idDocente: Number(idDocente), diaSemana, horaInicio, horaFin });

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
                this.cargarTodo();
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
            text: `${bloque.nombreDocente}: ${this.etiquetaDia(bloque.diaSemana)} de ${this.horaCorta(bloque.horaInicio)} a ${this.horaCorta(bloque.horaFin)}`,
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
                    this.cargarTodo();
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

    /** Ordena y filtra los bloques, y arma el calendario del docente elegido. */
    private recalcular(): void {
        const orden = DIAS_SEMANA.map(dia => dia.valor);

        this.visibles = this.bloques
            .filter(b => this.filtroDocente === null || b.idDocente === this.filtroDocente)
            .sort((a, b) => {
                const porDocente = (a.nombreDocente ?? '').localeCompare(b.nombreDocente ?? '');
                if (porDocente !== 0) {
                    return porDocente;
                }
                const porDia = orden.indexOf(a.diaSemana) - orden.indexOf(b.diaSemana);
                if (porDia !== 0) {
                    return porDia;
                }
                return a.horaInicio.localeCompare(b.horaInicio);
            });

        let primera = 6;
        let ultima = 22;
        for (const bloque of this.visibles) {
            primera = Math.min(primera, Math.floor(this.aMinutos(bloque.horaInicio) / 60));
            ultima = Math.max(ultima, Math.ceil(this.aMinutos(bloque.horaFin) / 60));
        }
        this.horas = [];
        for (let hora = primera; hora < ultima; hora++) {
            this.horas.push(hora);
        }

        this.calendario = DIAS_SEMANA.map(dia => ({
            dia: dia.valor,
            etiqueta: dia.etiqueta,
            bloques: this.visibles.filter(bloque => bloque.diaSemana === dia.valor)
        }));
    }

    private aMinutos(hora: string): number {
        const partes = hora.split(':');
        return Number(partes[0]) * 60 + Number(partes[1]);
    }
}