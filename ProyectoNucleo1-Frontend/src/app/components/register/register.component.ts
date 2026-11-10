import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { UsuarioDTO } from '../../models/usuario-dto';

const PATRON_NOMBRE = '^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$';
const PATRON_USUARIO = '^[a-zA-Z0-9._-]+$';
const PATRON_TELEFONO = '^[0-9]{7,15}$';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  form: FormGroup;
  cargando = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private errorHandler: ErrorHandlerService
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2), Validators.pattern(PATRON_NOMBRE)]],
      apellido: ['', [Validators.required, Validators.minLength(2), Validators.pattern(PATRON_NOMBRE)]],
      correo: ['', [Validators.required, Validators.email]],
      nombreUsuario: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40), Validators.pattern(PATRON_USUARIO)]],
      telefono: ['', [Validators.required, Validators.pattern(PATRON_TELEFONO)]],
      contrasena: ['', [Validators.required, Validators.minLength(8)]],
      confirmarContrasena: ['', [Validators.required]]
    }, { validators: this.contrasenasIguales });
  }

  private contrasenasIguales(group: AbstractControl): ValidationErrors | null {
    const contrasena = group.get('contrasena')?.value;
    const confirmar = group.get('confirmarContrasena')?.value;
    if (contrasena && confirmar && contrasena !== confirmar) {
      return { noCoinciden: true };
    }
    return null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error = 'Revisa los campos marcados antes de continuar.';
      return;
    }

    this.cargando = true;
    this.error = '';

    const valores = this.form.value;
    const contrasena = valores.contrasena as string;
    const usuario: UsuarioDTO = {
      nombre: valores.nombre.trim(),
      apellido: valores.apellido.trim(),
      correo: valores.correo.trim(),
      nombreUsuario: valores.nombreUsuario.trim(),
      contrasena,
      telefono: valores.telefono.trim()
    };

    this.auth.registrar(usuario).subscribe({
      next: () => {
        this.auth.login(usuario.nombreUsuario, contrasena).subscribe({
          next: () => {
            this.cargando = false;
            this.router.navigate(['/dashboard']);
          },
          error: () => {
            this.cargando = false;
            this.router.navigate(['/login']);
          }
        });
      },
      error: (err) => {
        this.cargando = false;
        this.error = this.errorHandler.extraerMensaje(err, 'No fue posible completar el registro.');
      }
    });
  }
}
