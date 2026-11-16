import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import Swal from 'sweetalert2';

import { UsuarioService } from '../../services/usuario.service';
import { DisponibilidadService } from '../../services/disponibilidad.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';
import { UsuarioDTO } from '../../models/usuario-dto';

type Rol = 'DOCENTE' | 'ADMIN';

const PATRON_NOMBRE = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]{2,60}$/;
const PATRON_CORREO = /^[\w.+-]+@[\w.-]+\.[a-zA-Z]{2,}$/;
const PATRON_TELEFONO = /^[0-9]{7,15}$/;

@Component({
    selector: 'admin-usuarios',
    templateUrl: './admin-usuarios.component.html',
    styleUrls: ['./admin-usuarios.component.css']
})
export class AdminUsuariosComponent implements OnInit {

    readonly roles: { valor: Rol; etiqueta: string }[] = [
        { valor: 'DOCENTE', etiqueta: 'Docente' },
        { valor: 'ADMIN', etiqueta: 'Administrador' }
    ];

    usuarios: UsuarioDTO[] = [];
    cargando = true;
    guardando = false;
    error = '';
    editando: UsuarioDTO | null = null;
    mostrarContrasena = false;

    busqueda = '';
    filtroRol: '' | Rol = '';

    form: FormGroup;

    constructor(
        private fb: FormBuilder,
        private usuarioService: UsuarioService,
        private disponibilidad: DisponibilidadService,
        private errorHandler: ErrorHandlerService,
        private auth: AuthService
    ) {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.pattern(PATRON_NOMBRE)]],
            apellido: ['', [Validators.required, Validators.pattern(PATRON_NOMBRE)]],
            correo: ['', [Validators.required, Validators.pattern(PATRON_CORREO)]],
            nombreUsuario: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
            telefono: ['', [Validators.pattern(PATRON_TELEFONO)]],
            rol: ['DOCENTE', Validators.required],
            contrasena: ['', [Validators.required, Validators.minLength(8)]]
        });
    }

    ngOnInit(): void {
        this.cargarUsuarios();
    }

    get totalDocentes(): number {
        return this.usuarios.filter(u => u.rol === 'DOCENTE').length;
    }

    get totalAdministradores(): number {
        return this.usuarios.filter(u => u.rol === 'ADMIN').length;
    }

    get usuariosFiltrados(): UsuarioDTO[] {
        const texto = this.busqueda.trim().toLowerCase();
        return this.usuarios.filter(u => {
            if (this.filtroRol && u.rol !== this.filtroRol) {
                return false;
            }
            if (!texto) {
                return true;
            }
            return [u.nombre, u.apellido, u.correo, u.nombreUsuario]
                .some(campo => (campo ?? '').toLowerCase().includes(texto));
        });
    }

    esYo(usuario: UsuarioDTO): boolean {
        return usuario.id !== undefined && usuario.id === this.auth.getCurrentUser()?.id;
    }

    etiquetaRol(rol?: Rol): string {
        return rol === 'ADMIN' ? 'Administrador' : 'Docente';
    }

    toggleContrasena(): void {
        this.mostrarContrasena = !this.mostrarContrasena;
    }

    campoInvalido(nombre: string): boolean {
        const control = this.form.get(nombre);
        return !!control && control.invalid && (control.touched || control.dirty);
    }

    cargarUsuarios(): void {
        this.cargando = true;
        this.error = '';

        this.usuarioService.listar().subscribe({
            next: (usuarios) => {
                this.usuarios = [...usuarios].sort((a, b) => (a.id ?? 0) - (b.id ?? 0));
                this.cargando = false;
            },
            error: (err) => {
                this.cargando = false;
                this.error = this.errorHandler.extraerMensaje(err, 'No fue posible cargar los usuarios.');
            }
        });
    }

    editar(usuario: UsuarioDTO): void {
        if (usuario.id === undefined) {
            return;
        }
        this.editando = usuario;
        this.mostrarContrasena = false;

        this.form.reset({
            nombre: usuario.nombre,
            apellido: usuario.apellido,
            correo: usuario.correo,
            nombreUsuario: usuario.nombreUsuario,
            telefono: usuario.telefono ?? '',
            rol: usuario.rol ?? 'DOCENTE',
            contrasena: ''
        });

        // Al editar, la contraseña es opcional: si queda vacía se conserva la actual.
        this.form.get('contrasena')?.setValidators([Validators.minLength(8)]);
        this.form.get('contrasena')?.updateValueAndValidity();

        // El administrador no puede quitarse el rol ni cambiar su usuario: perdería su propia sesión.
        if (this.esYo(usuario)) {
            this.form.get('rol')?.disable();
            this.form.get('nombreUsuario')?.disable();
        } else {
            this.form.get('rol')?.enable();
            this.form.get('nombreUsuario')?.enable();
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    cancelarEdicion(): void {
        this.editando = null;
        this.mostrarContrasena = false;
        this.form.get('rol')?.enable();
        this.form.get('nombreUsuario')?.enable();
        this.form.get('contrasena')?.setValidators([Validators.required, Validators.minLength(8)]);
        this.form.reset({
            nombre: '', apellido: '', correo: '', nombreUsuario: '', telefono: '', rol: 'DOCENTE', contrasena: ''
        });
    }

    guardar(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            Swal.fire('Faltan datos', 'Revisa los campos marcados e intenta de nuevo.', 'warning');
            return;
        }

        const v = this.form.getRawValue();
        const payload: UsuarioDTO = {
            nombre: v.nombre.trim(),
            apellido: v.apellido.trim(),
            correo: v.correo.trim(),
            nombreUsuario: v.nombreUsuario.trim(),
            rol: v.rol
        };
        if (v.telefono && v.telefono.trim()) {
            payload.telefono = v.telefono.trim();
        }
        if (v.contrasena) {
            payload.contrasena = v.contrasena;
        }

        const estabaEditando = this.editando !== null;
        const editado = this.editando;
        this.guardando = true;

        const peticion = editado && editado.id !== undefined
            ? this.usuarioService.actualizar(editado.id, payload)
            : this.usuarioService.crear(payload);

        peticion.subscribe({
            next: (guardado) => {
                this.guardando = false;
                if (editado && this.esYo(editado)) {
                    this.auth.actualizarUsuarioActual(guardado);
                }
                Swal.fire({
                    icon: 'success',
                    title: estabaEditando ? 'Usuario actualizado' : 'Usuario creado',
                    timer: 1600,
                    showConfirmButton: false
                });
                this.cancelarEdicion();
                this.cargarUsuarios();
            },
            error: (err) => {
                this.guardando = false;
                Swal.fire('No se guardó el usuario', this.errorHandler.extraerMensaje(err, 'No fue posible guardar el usuario.'), 'error');
            }
        });
    }

    eliminar(usuario: UsuarioDTO): void {
        if (usuario.id === undefined || this.esYo(usuario)) {
            return;
        }
        const id = usuario.id;

        // Se consultan sus bloques antes de confirmar: la base de datos no permite borrar
        // un usuario que todavía tiene bloques asociados.
        this.disponibilidad.consultarDocente(id).subscribe({
            next: (bloques) => {
                const aviso = bloques.length > 0
                    ? `También se eliminarán sus ${bloques.length} bloque(s) de disponibilidad. Esta acción no se puede deshacer.`
                    : 'Esta acción no se puede deshacer.';

                Swal.fire({
                    icon: 'question',
                    title: `¿Eliminar a ${usuario.nombre} ${usuario.apellido}?`,
                    text: aviso,
                    showCancelButton: true,
                    confirmButtonText: 'Sí, eliminar',
                    cancelButtonText: 'Cancelar',
                    confirmButtonColor: '#c0392b'
                }).then(resultado => {
                    if (!resultado.isConfirmed) {
                        return;
                    }

                    const borrados = bloques
                        .filter(b => b.id !== undefined)
                        .map(b => this.disponibilidad.eliminarBloque(b.id as number));

                    (borrados.length > 0 ? forkJoin(borrados) : of([]))
                        .pipe(switchMap(() => this.usuarioService.eliminar(id)))
                        .subscribe({
                            next: () => {
                                Swal.fire({ icon: 'success', title: 'Usuario eliminado', timer: 1600, showConfirmButton: false });
                                if (this.editando?.id === id) {
                                    this.cancelarEdicion();
                                }
                                this.cargarUsuarios();
                            },
                            error: (err) => {
                                Swal.fire('No se eliminó el usuario', this.errorHandler.extraerMensaje(err, 'No fue posible eliminar el usuario.'), 'error');
                                this.cargarUsuarios();
                            }
                        });
                });
            },
            error: (err) => {
                Swal.fire('No se eliminó el usuario', this.errorHandler.extraerMensaje(err, 'No fue posible eliminar el usuario.'), 'error');
            }
        });
    }
}
