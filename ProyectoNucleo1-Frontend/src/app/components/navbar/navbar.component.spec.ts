import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { NavbarComponent } from './navbar.component';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';

describe('NavbarComponent', () => {
    let component: NavbarComponent;
    let fixture: ComponentFixture<NavbarComponent>;
    let auth: jasmine.SpyObj<AuthService>;
    let theme: jasmine.SpyObj<ThemeService>;
    let router: Router;

    beforeEach(async () => {
        auth = jasmine.createSpyObj<AuthService>('AuthService', ['logout', 'clearLocal', 'isLoggedIn', 'getCurrentUser', 'isAdmin']);
        theme = jasmine.createSpyObj<ThemeService>('ThemeService', ['isDark', 'toggle']);
        auth.isLoggedIn.and.returnValue(false);
        auth.getCurrentUser.and.returnValue(null);
        auth.isAdmin.and.returnValue(false);
        theme.isDark.and.returnValue(false);

        await TestBed.configureTestingModule({
            declarations: [NavbarComponent],
            imports: [RouterTestingModule],
            providers: [
                { provide: AuthService, useValue: auth },
                { provide: ThemeService, useValue: theme }
            ]
        }).compileComponents();

        router = TestBed.inject(Router);
        spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

        fixture = TestBed.createComponent(NavbarComponent);
        component = fixture.componentInstance;
    });

    /** Busca un botón por su texto visible. */
    function botonConTexto(texto: string): HTMLButtonElement | undefined {
        const botones = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
        return botones.find(b => b.textContent?.trim() === texto);
    }

    /** Busca un enlace por su texto visible. */
    function enlaceConTexto(texto: string): HTMLAnchorElement | undefined {
        const enlaces = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
        return enlaces.find(a => a.textContent?.trim() === texto);
    }

    it('se crea correctamente', () => {
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('inicia con el menú móvil cerrado', () => {
        expect(component.menuAbierto).toBeFalse();
    });

    it('cerrarMenu() cierra el menú móvil', () => {
        component.menuAbierto = true;
        component.cerrarMenu();
        expect(component.menuAbierto).toBeFalse();
    });

    describe('logout()', () => {
        it('navega a "/" tras cerrar sesión correctamente', () => {
            auth.logout.and.returnValue(of({}));

            component.logout();

            expect(auth.logout).toHaveBeenCalled();
            expect(router.navigate).toHaveBeenCalledWith(['/']);
            expect(auth.clearLocal).not.toHaveBeenCalled();
        });

        it('si el servidor falla, limpia la sesión local y navega a "/"', () => {
            auth.logout.and.returnValue(throwError(() => new Error('sin conexión')));

            component.logout();

            expect(auth.clearLocal).toHaveBeenCalled();
            expect(router.navigate).toHaveBeenCalledWith(['/']);
        });
    });

    describe('sin sesión', () => {
        it('muestra los accesos "Acceder" y "Registrarse"', () => {
            fixture.detectChanges();

            expect(enlaceConTexto('Acceder')).toBeDefined();
            expect(enlaceConTexto('Registrarse')).toBeDefined();
            expect(botonConTexto('Salir')).toBeUndefined();
        });

        it('no muestra el enlace "Mis bloques"', () => {
            fixture.detectChanges();
            expect(enlaceConTexto('Mis bloques')).toBeUndefined();
        });

        it('no muestra el enlace "Panel de administración"', () => {
            fixture.detectChanges();
            expect(enlaceConTexto('Panel de administración')).toBeUndefined();
        });

        it('siempre muestra "Inicio" y "Consultar disponibilidad"', () => {
            fixture.detectChanges();

            expect(enlaceConTexto('Inicio')).toBeDefined();
            expect(enlaceConTexto('Consultar disponibilidad')).toBeDefined();
        });
    });

    describe('con sesión activa', () => {
        beforeEach(() => {
            auth.isLoggedIn.and.returnValue(true);
            auth.getCurrentUser.and.returnValue({
                nombre: 'Ana', apellido: 'Gómez', correo: 'ana@correo.com', nombreUsuario: 'ana'
            });
        });

        it('muestra el nombre completo del usuario', () => {
            fixture.detectChanges();
            expect((fixture.nativeElement as HTMLElement).textContent).toContain('Ana Gómez');
        });

        it('muestra "Mis bloques" y el botón "Salir", y oculta "Acceder"', () => {
            fixture.detectChanges();

            expect(enlaceConTexto('Mis bloques')).toBeDefined();
            expect(botonConTexto('Salir')).toBeDefined();
            expect(enlaceConTexto('Acceder')).toBeUndefined();
        });

        it('un docente no ve el enlace "Panel de administración"', () => {
            fixture.detectChanges();
            expect(enlaceConTexto('Panel de administración')).toBeUndefined();
        });

        it('un administrador ve "Panel de administración" en lugar de "Mis bloques"', () => {
            auth.isAdmin.and.returnValue(true);
            fixture.detectChanges();

            expect(enlaceConTexto('Panel de administración')).toBeDefined();
            expect(enlaceConTexto('Mis bloques')).toBeUndefined();
        });

        it('al hacer clic en "Salir" cierra sesión y navega a "/"', () => {
            auth.logout.and.returnValue(of({}));
            fixture.detectChanges();

            botonConTexto('Salir')?.click();

            expect(auth.logout).toHaveBeenCalled();
            expect(router.navigate).toHaveBeenCalledWith(['/']);
        });
    });

    describe('tema y menú móvil', () => {
        it('el botón de tema invoca theme.toggle()', () => {
            fixture.detectChanges();

            (fixture.nativeElement.querySelector('.tema') as HTMLButtonElement).click();

            expect(theme.toggle).toHaveBeenCalled();
        });

        it('el botón hamburguesa abre y cierra el menú móvil', () => {
            fixture.detectChanges();
            const hamburguesa = fixture.nativeElement.querySelector('.hamburguesa') as HTMLButtonElement;
            expect(fixture.nativeElement.querySelector('.menu-movil')).toBeNull();

            hamburguesa.click();
            fixture.detectChanges();
            expect(component.menuAbierto).toBeTrue();
            expect(fixture.nativeElement.querySelector('.menu-movil')).not.toBeNull();

            hamburguesa.click();
            fixture.detectChanges();
            expect(component.menuAbierto).toBeFalse();
            expect(fixture.nativeElement.querySelector('.menu-movil')).toBeNull();
        });
    });
});