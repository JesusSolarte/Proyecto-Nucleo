import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Subject, of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthResponse } from '../../models/auth-response';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
    let router: Router;

    const respuesta: AuthResponse = {
        token: 'jwt.token.firma',
        tipoToken: 'Bearer',
        usuario: { id: 1, nombre: 'Ana', apellido: 'García', correo: 'ana@correo.com', nombreUsuario: 'ana.garcia' }
    };

    beforeEach(async () => {
        auth = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'isAdmin']);
        errorHandler = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['extraerMensaje']);

        await TestBed.configureTestingModule({
            declarations: [LoginComponent],
            imports: [ReactiveFormsModule, RouterTestingModule],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: ErrorHandlerService, useValue: errorHandler }
            ]
        }).compileComponents();

        router = TestBed.inject(Router);
        spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('se crea correctamente', () => {
        expect(component).toBeTruthy();
    });

    describe('formulario', () => {
        it('es inválido cuando está vacío', () => {
            expect(component.form.valid).toBeFalse();
        });

        it('es válido con usuario (>= 3) y contraseña (>= 8)', () => {
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'Clave1234' });
            expect(component.form.valid).toBeTrue();
        });

        it('rechaza un usuario de menos de 3 caracteres', () => {
            component.form.setValue({ nombreUsuario: 'an', contrasena: 'Clave1234' });
            expect(component.form.get('nombreUsuario')?.hasError('minlength')).toBeTrue();
        });

        it('rechaza una contraseña de menos de 8 caracteres', () => {
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'corta' });
            expect(component.form.get('contrasena')?.hasError('minlength')).toBeTrue();
        });

        it('el botón de envío está deshabilitado mientras el formulario es inválido', () => {
            const boton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
            expect(boton.disabled).toBeTrue();

            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'Clave1234' });
            fixture.detectChanges();

            expect(boton.disabled).toBeFalse();
        });
    });

    describe('toggleContrasena()', () => {
        it('alterna la visibilidad de la contraseña', () => {
            expect(component.mostrarContrasena).toBeFalse();
            component.toggleContrasena();
            expect(component.mostrarContrasena).toBeTrue();
            component.toggleContrasena();
            expect(component.mostrarContrasena).toBeFalse();
        });
    });

    describe('submit()', () => {
        it('con formulario inválido muestra el error, marca los campos y no llama al servicio', () => {
            component.submit();

            expect(component.error).toBe('Por favor completa todos los campos.');
            expect(component.form.get('nombreUsuario')?.touched).toBeTrue();
            expect(auth.login).not.toHaveBeenCalled();
            expect(component.cargando).toBeFalse();
        });

        it('envía el usuario sin espacios sobrantes y la contraseña tal cual', () => {
            auth.login.and.returnValue(of(respuesta));
            auth.isAdmin.and.returnValue(false);
            component.form.setValue({ nombreUsuario: '  ana.garcia  ', contrasena: 'Clave1234' });

            component.submit();

            expect(auth.login).toHaveBeenCalledWith('ana.garcia', 'Clave1234');
        });

        it('redirige a /dashboard cuando el usuario no es administrador', () => {
            auth.login.and.returnValue(of(respuesta));
            auth.isAdmin.and.returnValue(false);
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'Clave1234' });

            component.submit();

            expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
            expect(component.cargando).toBeFalse();
            expect(component.error).toBe('');
        });

        it('redirige a /admin cuando el usuario es administrador', () => {
            auth.login.and.returnValue(of(respuesta));
            auth.isAdmin.and.returnValue(true);
            component.form.setValue({ nombreUsuario: 'admin', contrasena: 'Admin12345' });

            component.submit();

            expect(router.navigate).toHaveBeenCalledWith(['/admin']);
        });

        it('mantiene cargando=true mientras la petición está en curso', () => {
            const peticion = new Subject<AuthResponse>();
            auth.login.and.returnValue(peticion.asObservable());
            auth.isAdmin.and.returnValue(false);
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'Clave1234' });

            component.submit();

            expect(component.cargando).toBeTrue();
            expect(router.navigate).not.toHaveBeenCalled();

            peticion.next(respuesta);
            peticion.complete();

            expect(component.cargando).toBeFalse();
        });

        it('ante un error usa ErrorHandlerService, muestra el mensaje y no navega', () => {
            const fallo = new Error('401');
            auth.login.and.returnValue(throwError(() => fallo));
            errorHandler.extraerMensaje.and.returnValue('Usuario o contraseña incorrectos.');
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'incorrecta1' });

            component.submit();

            expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'Usuario o contraseña incorrectos.');
            expect(component.error).toBe('Usuario o contraseña incorrectos.');
            expect(component.cargando).toBeFalse();
            expect(router.navigate).not.toHaveBeenCalled();
        });

        it('limpia el error anterior al reintentar', () => {
            auth.login.and.returnValue(of(respuesta));
            auth.isAdmin.and.returnValue(false);
            component.error = 'Error previo';
            component.form.setValue({ nombreUsuario: 'ana', contrasena: 'Clave1234' });

            component.submit();

            expect(component.error).toBe('');
        });
    });

    describe('vista', () => {
        it('muestra el mensaje de error en pantalla', () => {
            component.error = 'Usuario o contraseña incorrectos.';
            fixture.detectChanges();

            const aviso = fixture.nativeElement.querySelector('.aviso-error') as HTMLElement;
            expect(aviso.textContent).toContain('Usuario o contraseña incorrectos.');
        });

        it('no muestra el aviso de error cuando no hay error', () => {
            expect(fixture.nativeElement.querySelector('.aviso-error')).toBeNull();
        });
    });
});