import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConsultaComponent } from './components/consulta/consulta.component';
import { AdminComponent } from './components/admin/admin.component';
import { AdminUsuariosComponent } from './components/admin-usuarios/admin-usuarios.component';
import { AdminBloquesComponent } from './components/admin-bloques/admin-bloques.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { DocenteGuard } from './guards/docente.guard';

const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard, DocenteGuard] },
    { path: 'consulta', component: ConsultaComponent },
    {
        path: 'admin',
        component: AdminComponent,
        canActivate: [AuthGuard, AdminGuard],
        children: [
            { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
            { path: 'usuarios', component: AdminUsuariosComponent },
            { path: 'disponibilidad', component: AdminBloquesComponent }
        ]
    },
    { path: '**', redirectTo: '' }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {}