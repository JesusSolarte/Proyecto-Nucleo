import { TestBed } from '@angular/core/testing';
import { Router, Route } from '@angular/router';

import { AppRoutingModule } from './app-routing.module';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { DocenteGuard } from './guards/docente.guard';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConsultaComponent } from './components/consulta/consulta.component';
import { AdminComponent } from './components/admin/admin.component';
import { AdminUsuariosComponent } from './components/admin-usuarios/admin-usuarios.component';
import { AdminBloquesComponent } from './components/admin-bloques/admin-bloques.component';

describe('AppRoutingModule', () => {
    let rutas: Route[];

    function ruta(path: string): Route | undefined {
        return rutas.find(r => r.path === path);
    }

    beforeEach(() => {
        TestBed.configureTestingModule({ imports: [AppRoutingModule] });
        rutas = TestBed.inject(Router).config;
    });

    it('define las rutas esperadas', () => {
        expect(rutas.map(r => r.path)).toEqual(['', 'login', 'register', 'dashboard', 'consulta', 'admin', '**']);
    });

    it('asocia cada ruta con su componente', () => {
        expect(ruta('')?.component).toBe(HomeComponent);
        expect(ruta('login')?.component).toBe(LoginComponent);
        expect(ruta('register')?.component).toBe(RegisterComponent);
        expect(ruta('dashboard')?.component).toBe(DashboardComponent);
        expect(ruta('consulta')?.component).toBe(ConsultaComponent);
        expect(ruta('admin')?.component).toBe(AdminComponent);
    });

    it('protege /dashboard con AuthGuard', () => {
        expect(ruta('dashboard')?.canActivate).toContain(AuthGuard);
    });

    it('el dashboard es solo para docentes: el administrador va a su panel', () => {
        expect(ruta('dashboard')?.canActivate).toContain(DocenteGuard);
    });

    it('protege /admin con AuthGuard y AdminGuard', () => {
        expect(ruta('admin')?.canActivate).toContain(AuthGuard);
        expect(ruta('admin')?.canActivate).toContain(AdminGuard);
    });

    it('/admin tiene las secciones usuarios y disponibilidad, y abre usuarios por defecto', () => {
        const hijas = ruta('admin')?.children ?? [];
        expect(hijas.map(r => r.path)).toEqual(['', 'usuarios', 'disponibilidad']);
        expect(hijas[0].redirectTo).toBe('usuarios');
        expect(hijas[1].component).toBe(AdminUsuariosComponent);
        expect(hijas[2].component).toBe(AdminBloquesComponent);
    });

    it('las rutas públicas no tienen guard', () => {
        for (const path of ['', 'login', 'register', 'consulta']) {
            expect(ruta(path)?.canActivate).toBeUndefined();
        }
    });

    it('redirige cualquier ruta desconocida al inicio', () => {
        expect(ruta('**')?.redirectTo).toBe('');
    });
});