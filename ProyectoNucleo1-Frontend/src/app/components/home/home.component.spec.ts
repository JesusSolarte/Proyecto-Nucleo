import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { HomeComponent } from './home.component';
import { AuthService } from '../../services/auth.service';

describe('HomeComponent', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;
    let auth: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        auth = jasmine.createSpyObj<AuthService>('AuthService', ['isLoggedIn', 'isAdmin']);
        auth.isLoggedIn.and.returnValue(false);
        auth.isAdmin.and.returnValue(false);

        await TestBed.configureTestingModule({
            declarations: [HomeComponent],
            imports: [RouterTestingModule],
            providers: [{ provide: AuthService, useValue: auth }]
        }).compileComponents();

        fixture = TestBed.createComponent(HomeComponent);
        component = fixture.componentInstance;
    });

    function textoPagina(): string {
        return (fixture.nativeElement as HTMLElement).textContent ?? '';
    }

    it('se crea correctamente', () => {
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('expone el AuthService como propiedad pública para el template', () => {
        expect(component.auth).toBe(auth);
    });

    it('sin sesión invita a "Iniciar sesión" y no muestra "Ir a mis bloques"', () => {
        auth.isLoggedIn.and.returnValue(false);
        fixture.detectChanges();

        expect(textoPagina()).toContain('Iniciar sesión');
        expect(textoPagina()).not.toContain('Ir a mis bloques');
    });

    it('con sesión muestra "Ir a mis bloques" y no "Iniciar sesión"', () => {
        auth.isLoggedIn.and.returnValue(true);
        fixture.detectChanges();

        expect(textoPagina()).toContain('Ir a mis bloques');
        expect(textoPagina()).not.toContain('Iniciar sesión');
    });

    it('un administrador con sesión ve "Ir al panel de administración" y no "Ir a mis bloques"', () => {
        auth.isLoggedIn.and.returnValue(true);
        auth.isAdmin.and.returnValue(true);
        fixture.detectChanges();

        expect(textoPagina()).toContain('Ir al panel de administración');
        expect(textoPagina()).not.toContain('Ir a mis bloques');
    });

    it('siempre ofrece consultar la disponibilidad', () => {
        fixture.detectChanges();
        expect(textoPagina()).toContain('Consultar disponibilidad');
    });
});