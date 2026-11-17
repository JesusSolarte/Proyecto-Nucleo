import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthResponse } from '../../models/auth-response';
import { UsuarioDTO } from '../../models/usuario-dto';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let auth: jasmine.SpyObj<AuthService>;
  let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
  let router: Router;

  const formularioValido = {
    nombre: 'Laura',
    apellido: 'Soto',
    correo: 'laura@unbosque.edu.co',
    nombreUsuario: 'laura.soto',
    telefono: '3001234567',
    contrasena: 'Clave1234',
    confirmarContrasena: 'Clave1234'
  };

  const usuarioCreado: UsuarioDTO = {
    id: 5,
    nombre: 'Laura',
    apellido: 'Soto',
    correo: 'laura@unbosque.edu.co',
    nombreUsuario: 'laura.soto'
  };

  const respuestaLogin: AuthResponse = { token: 't', tipoToken: 'Bearer', usuario: usuarioCreado };

  beforeEach(async () => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['registrar', 'login']);
    errorHandler = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['extraerMensaje']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: ErrorHandlerService, useValue: errorHandler }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('se crea correctamente', () => {
    expect(component).toBeTruthy();
  });

  describe('validaciones del formulario', () => {
    it('es inválido cuando está vacío', () => {
      expect(component.form.valid).toBeFalse();
    });

    it('es válido con todos los datos correctos', () => {
      component.form.setValue(formularioValido);
      expect(component.form.valid).toBeTrue();
    });

    it('el teléfono es opcional', () => {
      component.form.setValue({ ...formularioValido, telefono: '' });
      expect(component.form.valid).toBeTrue();
    });

    it('rechaza un nombre con números', () => {
      component.form.setValue({ ...formularioValido, nombre: 'Laura2' });
      expect(component.form.get('nombre')?.hasError('pattern')).toBeTrue();
    });

    it('acepta nombres con tildes, ñ y espacios', () => {
      component.form.setValue({ ...formularioValido, nombre: 'María José', apellido: 'Muñoz Peña' });
      expect(component.form.get('nombre')?.valid).toBeTrue();
      expect(component.form.get('apellido')?.valid).toBeTrue();
    });

    it('rechaza un nombre de un solo carácter', () => {
      component.form.setValue({ ...formularioValido, nombre: 'L' });
      expect(component.form.get('nombre')?.hasError('minlength')).toBeTrue();
    });

    it('rechaza un correo sin formato válido', () => {
      component.form.setValue({ ...formularioValido, correo: 'no-es-correo' });
      expect(component.form.get('correo')?.hasError('email')).toBeTrue();
    });

    it('rechaza un nombre de usuario con espacios o símbolos no permitidos', () => {
      component.form.setValue({ ...formularioValido, nombreUsuario: 'laura soto!' });
      expect(component.form.get('nombreUsuario')?.hasError('pattern')).toBeTrue();
    });

    it('rechaza un teléfono con letras o demasiado corto', () => {
      component.form.setValue({ ...formularioValido, telefono: '30012abc' });
      expect(component.form.get('telefono')?.hasError('pattern')).toBeTrue();

      component.form.setValue({ ...formularioValido, telefono: '12345' });
      expect(component.form.get('telefono')?.hasError('pattern')).toBeTrue();
    });

    it('rechaza una contraseña de menos de 8 caracteres', () => {
      component.form.setValue({ ...formularioValido, contrasena: 'corta', confirmarContrasena: 'corta' });
      expect(component.form.get('contrasena')?.hasError('minlength')).toBeTrue();
    });

    it('marca error "noCoinciden" cuando las contraseñas son distintas', () => {
      component.form.setValue({ ...formularioValido, confirmarContrasena: 'OtraClave99' });

      expect(component.form.hasError('noCoinciden')).toBeTrue();
      expect(component.form.valid).toBeFalse();
    });

    it('no marca "noCoinciden" cuando las contraseñas son iguales', () => {
      component.form.setValue(formularioValido);
      expect(component.form.hasError('noCoinciden')).toBeFalse();
    });
  });

  describe('submit()', () => {
    it('con formulario inválido muestra el error y no llama al servicio', () => {
      component.submit();

      expect(component.error).toBe('Revisa los campos marcados antes de continuar.');
      expect(component.form.get('nombre')?.touched).toBeTrue();
      expect(auth.registrar).not.toHaveBeenCalled();
      expect(component.cargando).toBeFalse();
    });

    it('registra al usuario con los datos recortados y sin confirmarContrasena', () => {
      auth.registrar.and.returnValue(of(usuarioCreado));
      auth.login.and.returnValue(of(respuestaLogin));
      component.form.setValue({ ...formularioValido, nombre: '  Laura ', apellido: ' Soto  ' });

      component.submit();

      expect(auth.registrar).toHaveBeenCalledWith({
        nombre: 'Laura',
        apellido: 'Soto',
        correo: 'laura@unbosque.edu.co',
        nombreUsuario: 'laura.soto',
        contrasena: 'Clave1234',
        telefono: '3001234567'
      });
    });

    it('tras registrar inicia sesión automáticamente y navega a /dashboard', () => {
      auth.registrar.and.returnValue(of(usuarioCreado));
      auth.login.and.returnValue(of(respuestaLogin));
      component.form.setValue(formularioValido);

      component.submit();

      expect(auth.login).toHaveBeenCalledWith('laura.soto', 'Clave1234');
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
      expect(component.cargando).toBeFalse();
      expect(component.error).toBe('');
    });

    it('si el registro fue exitoso pero el login falla, navega a /login', () => {
      auth.registrar.and.returnValue(of(usuarioCreado));
      auth.login.and.returnValue(throwError(() => new Error('fallo login')));
      component.form.setValue(formularioValido);

      component.submit();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      expect(component.cargando).toBeFalse();
    });

    it('si el registro falla muestra el mensaje del ErrorHandlerService y no hace login', () => {
      const fallo = new Error('409');
      auth.registrar.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('El usuario ya existe.');
      component.form.setValue(formularioValido);

      component.submit();

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible completar el registro.');
      expect(component.error).toBe('El usuario ya existe.');
      expect(component.cargando).toBeFalse();
      expect(auth.login).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('vista', () => {
    it('muestra el mensaje de error en pantalla', () => {
      component.error = 'El usuario ya existe.';
      fixture.detectChanges();

      const aviso = fixture.nativeElement.querySelector('.aviso-error') as HTMLElement;
      expect(aviso.textContent).toContain('El usuario ya existe.');
    });
  });
});
