import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import Swal from 'sweetalert2';

import { DashboardComponent } from './dashboard.component';
import { DisponibilidadService } from '../../services/disponibilidad.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { AuthService } from '../../services/auth.service';
import { BloqueDisponibilidadDTO } from '../../models/bloque-disponibilidad-dto';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let disponibilidad: jasmine.SpyObj<DisponibilidadService>;
  let auth: jasmine.SpyObj<AuthService>;
  let errorHandler: jasmine.SpyObj<ErrorHandlerService>;
  let swal: jasmine.Spy;

  // 08-10 (120 min) + 07-08:30 (90 min) + 14-16 (120 min) = 330 min = 5 h 30 min
  const bloques: BloqueDisponibilidadDTO[] = [
    { id: 1, diaSemana: 'LUNES', horaInicio: '08:00:00', horaFin: '10:00:00' },
    { id: 2, diaSemana: 'LUNES', horaInicio: '07:00:00', horaFin: '08:30:00' },
    { id: 3, diaSemana: 'MIERCOLES', horaInicio: '14:00:00', horaFin: '16:00:00' }
  ];

  beforeEach(async () => {
    disponibilidad = jasmine.createSpyObj<DisponibilidadService>('DisponibilidadService', [
      'listarMisBloques',
      'crearBloque',
      'actualizarBloque',
      'eliminarBloque'
    ]);
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['getCurrentUser']);
    errorHandler = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['extraerMensaje']);

    disponibilidad.listarMisBloques.and.returnValue(of(bloques));
    auth.getCurrentUser.and.returnValue({
      id: 1, nombre: 'Ana', apellido: 'García', correo: 'ana@correo.com', nombreUsuario: 'ana.garcia'
    });

    // Evita ventanas emergentes reales y permite inspeccionar las llamadas a SweetAlert2.
    spyOn(Swal, 'fire').and.returnValue(Promise.resolve({ isConfirmed: true }) as any);
    swal = Swal.fire as unknown as jasmine.Spy;
    spyOn(window, 'scrollTo');

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: DisponibilidadService, useValue: disponibilidad },
        { provide: AuthService, useValue: auth },
        { provide: ErrorHandlerService, useValue: errorHandler }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('se crea correctamente', () => {
    expect(component).toBeTruthy();
  });

  describe('carga inicial', () => {
    it('ngOnInit() carga los bloques del docente', () => {
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(1);
      expect(component.bloques).toEqual(bloques);
      expect(component.cargando).toBeFalse();
      expect(component.error).toBe('');
    });

    it('el formulario inicia con LUNES, 08:00 y 10:00', () => {
      expect(component.form.value).toEqual({ diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });
    });

    it('saluda al usuario autenticado', () => {
      expect((fixture.nativeElement as HTMLElement).textContent).toContain('Hola, Ana');
    });

    it('cargarBloques() ante un error muestra el mensaje y deja de cargar', () => {
      const fallo = new Error('500');
      disponibilidad.listarMisBloques.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('No fue posible cargar tus bloques.');

      component.cargarBloques();

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible cargar tus bloques.');
      expect(component.error).toBe('No fue posible cargar tus bloques.');
      expect(component.cargando).toBeFalse();
    });
  });

  describe('calendario y estadísticas', () => {
    it('calendario agrupa los bloques por día (7 columnas)', () => {
      const calendario = component.calendario;

      expect(calendario.length).toBe(7);
      expect(calendario[0].bloques.length).toBe(2);
      expect(calendario[2].bloques.length).toBe(1);
    });

    it('bloquesOrdenados ordena por día y hora de inicio', () => {
      expect(component.bloquesOrdenados.map(b => b.id)).toEqual([2, 1, 3]);
    });

    it('diasConDisponibilidad cuenta los días con al menos un bloque', () => {
      expect(component.diasConDisponibilidad).toBe(2);
    });

    it('horasSemanales suma la duración de los bloques', () => {
      expect(component.horasSemanales).toBe('5 h 30 min');
    });

    it('horasSemanales muestra "0 h" sin bloques y "2 h" con un bloque de dos horas', () => {
      component.bloques = [];
      expect(component.horasSemanales).toBe('0 h');

      component.bloques = [bloques[0]];
      expect(component.horasSemanales).toBe('2 h');
    });

    it('horas cubre de 6 a 21 por defecto y se amplía si un bloque lo exige', () => {
      component.bloques = [];
      expect(component.horas.length).toBe(16);

      component.bloques = [{ id: 9, diaSemana: 'LUNES', horaInicio: '05:00:00', horaFin: '06:00:00' }];
      expect(component.horas[0]).toBe(5);

      component.bloques = [{ id: 9, diaSemana: 'LUNES', horaInicio: '20:00:00', horaFin: '23:30:00' }];
      expect(component.horas[component.horas.length - 1]).toBe(23);
    });

    it('arriba() y alto() usan 44 px por hora', () => {
      expect(component.arriba(bloques[0])).toBe(88);
      expect(component.alto(bloques[0])).toBe(88);
      expect(component.alto(bloques[1])).toBe(66);
    });

    it('horaCorta() y etiquetaDia() formatean para la vista', () => {
      expect(component.horaCorta('08:00:00')).toBe('08:00');
      expect(component.etiquetaDia('MARTES')).toBe('Martes');
      expect(component.etiquetaDia('XXX' as never)).toBe('XXX');
    });
  });

  describe('editar() y cancelarEdicion()', () => {
    it('editar() ignora bloques sin id', () => {
      component.editar({ diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });

      expect(component.editandoId).toBeNull();
    });

    it('editar() carga el bloque en el formulario con horas cortas y sube la página', () => {
      component.editar({ id: 3, diaSemana: 'MIERCOLES', horaInicio: '14:00:00', horaFin: '16:00:00' });

      expect(component.editandoId).toBe(3);
      expect(component.form.value).toEqual({ diaSemana: 'MIERCOLES', horaInicio: '14:00', horaFin: '16:00' });
      expect(window.scrollTo).toHaveBeenCalled();
    });

    it('cancelarEdicion() sale del modo edición y restaura los valores por defecto', () => {
      component.editar(bloques[2]);

      component.cancelarEdicion();

      expect(component.editandoId).toBeNull();
      expect(component.form.value).toEqual({ diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });
    });
  });

  describe('guardar()', () => {
    it('con formulario inválido avisa y no llama al servicio', () => {
      component.form.patchValue({ diaSemana: '' });

      component.guardar();

      expect(swal).toHaveBeenCalledWith('Faltan datos', jasmine.any(String), 'warning');
      expect(disponibilidad.crearBloque).not.toHaveBeenCalled();
      expect(disponibilidad.actualizarBloque).not.toHaveBeenCalled();
    });

    it('rechaza un rango donde el inicio es posterior al fin', () => {
      component.form.patchValue({ horaInicio: '10:00', horaFin: '08:00' });

      component.guardar();

      expect(swal).toHaveBeenCalledWith('Rango inválido', jasmine.any(String), 'warning');
      expect(disponibilidad.crearBloque).not.toHaveBeenCalled();
    });

    it('rechaza un rango donde inicio y fin son iguales', () => {
      component.form.patchValue({ horaInicio: '09:00', horaFin: '09:00' });

      component.guardar();

      expect(swal).toHaveBeenCalledWith('Rango inválido', jasmine.any(String), 'warning');
      expect(disponibilidad.crearBloque).not.toHaveBeenCalled();
    });

    it('crea un bloque nuevo, avisa del éxito y recarga la lista', () => {
      disponibilidad.crearBloque.and.returnValue(
        of({ id: 9, diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' })
      );

      component.guardar();

      expect(disponibilidad.crearBloque).toHaveBeenCalledWith({
        diaSemana: 'LUNES',
        horaInicio: '08:00',
        horaFin: '10:00'
      });
      expect(disponibilidad.actualizarBloque).not.toHaveBeenCalled();
      expect(swal).toHaveBeenCalledWith(jasmine.objectContaining({ icon: 'success', title: 'Bloque registrado' }));
      expect(component.guardando).toBeFalse();
      expect(component.editandoId).toBeNull();
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(2); // ngOnInit + recarga
    });

    it('en modo edición actualiza el bloque en lugar de crear uno nuevo', () => {
      disponibilidad.actualizarBloque.and.returnValue(
        of({ id: 5, diaSemana: 'MARTES', horaInicio: '09:00', horaFin: '11:00' })
      );
      component.editar({ id: 5, diaSemana: 'MARTES', horaInicio: '09:00:00', horaFin: '11:00:00' });

      component.guardar();

      expect(disponibilidad.actualizarBloque).toHaveBeenCalledWith(5, {
        diaSemana: 'MARTES',
        horaInicio: '09:00',
        horaFin: '11:00'
      });
      expect(disponibilidad.crearBloque).not.toHaveBeenCalled();
      expect(swal).toHaveBeenCalledWith(jasmine.objectContaining({ icon: 'success', title: 'Bloque actualizado' }));
      expect(component.editandoId).toBeNull();
    });

    it('ante un error del servidor muestra el mensaje y no recarga', () => {
      const fallo = new Error('409');
      disponibilidad.crearBloque.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('El bloque se cruza con otro.');

      component.guardar();

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible guardar el bloque.');
      expect(swal).toHaveBeenCalledWith('No se guardó el bloque', 'El bloque se cruza con otro.', 'error');
      expect(component.guardando).toBeFalse();
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(1);
    });
  });

  describe('eliminar()', () => {
    it('ignora bloques sin id', () => {
      component.eliminar({ diaSemana: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });

      expect(swal).not.toHaveBeenCalled();
      expect(disponibilidad.eliminarBloque).not.toHaveBeenCalled();
    });

    it('pide confirmación describiendo el bloque', fakeAsync(() => {
      disponibilidad.eliminarBloque.and.returnValue(of(undefined));

      component.eliminar(bloques[0]);
      tick();

      expect(swal.calls.argsFor(0)[0]).toEqual(
        jasmine.objectContaining({
          icon: 'question',
          title: '¿Eliminar este bloque?',
          text: 'Lunes de 08:00 a 10:00',
          showCancelButton: true
        })
      );
    }));

    it('si el usuario confirma, elimina el bloque y recarga la lista', fakeAsync(() => {
      disponibilidad.eliminarBloque.and.returnValue(of(undefined));

      component.eliminar(bloques[0]);
      tick();

      expect(disponibilidad.eliminarBloque).toHaveBeenCalledWith(1);
      expect(swal).toHaveBeenCalledWith(jasmine.objectContaining({ icon: 'success', title: 'Bloque eliminado' }));
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(2);
    }));

    it('si el usuario cancela, no elimina nada', fakeAsync(() => {
      swal.and.returnValue(Promise.resolve({ isConfirmed: false }));

      component.eliminar(bloques[0]);
      tick();

      expect(disponibilidad.eliminarBloque).not.toHaveBeenCalled();
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(1);
    }));

    it('si se estaba editando el bloque eliminado, sale del modo edición', fakeAsync(() => {
      disponibilidad.eliminarBloque.and.returnValue(of(undefined));
      component.editar(bloques[0]);
      expect(component.editandoId).toBe(1);

      component.eliminar(bloques[0]);
      tick();

      expect(component.editandoId).toBeNull();
    }));

    it('si se edita otro bloque, mantiene el modo edición', fakeAsync(() => {
      disponibilidad.eliminarBloque.and.returnValue(of(undefined));
      component.editar(bloques[2]);

      component.eliminar(bloques[0]);
      tick();

      expect(component.editandoId).toBe(3);
    }));

    it('ante un error del servidor muestra el mensaje', fakeAsync(() => {
      const fallo = new Error('500');
      disponibilidad.eliminarBloque.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('No se pudo eliminar.');

      component.eliminar(bloques[0]);
      tick();

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible eliminar el bloque.');
      expect(swal).toHaveBeenCalledWith('No se eliminó el bloque', 'No se pudo eliminar.', 'error');
      expect(disponibilidad.listarMisBloques).toHaveBeenCalledTimes(1);
    }));
  });
});
