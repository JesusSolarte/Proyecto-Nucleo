import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ConsultaComponent } from './consulta.component';
import { DisponibilidadService } from '../../services/disponibilidad.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { UsuarioDTO } from '../../models/usuario-dto';
import { BloqueDisponibilidadDTO } from '../../models/bloque-disponibilidad-dto';

describe('ConsultaComponent', () => {
  let component: ConsultaComponent;
  let fixture: ComponentFixture<ConsultaComponent>;
  let disponibilidad: jasmine.SpyObj<DisponibilidadService>;
  let errorHandler: jasmine.SpyObj<ErrorHandlerService>;

  const docentes: UsuarioDTO[] = [
    { id: 1, nombre: 'Ana', apellido: 'García', correo: '', nombreUsuario: '' },
    { id: 2, nombre: 'Luis', apellido: 'Ríos', correo: '', nombreUsuario: '' },
    { id: 3, nombre: 'María', apellido: 'Gómez', correo: '', nombreUsuario: '' }
  ];

  // 08-10 (120 min) + 07-08:30 (90 min) + 14-16 (120 min) = 330 min = 5 h 30 min
  const bloques: BloqueDisponibilidadDTO[] = [
    { id: 1, diaSemana: 'LUNES', horaInicio: '08:00:00', horaFin: '10:00:00' },
    { id: 2, diaSemana: 'LUNES', horaInicio: '07:00:00', horaFin: '08:30:00' },
    { id: 3, diaSemana: 'MIERCOLES', horaInicio: '14:00:00', horaFin: '16:00:00' }
  ];

  beforeEach(async () => {
    disponibilidad = jasmine.createSpyObj<DisponibilidadService>('DisponibilidadService', [
      'listarDocentes',
      'consultarDocente'
    ]);
    errorHandler = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['extraerMensaje']);
    disponibilidad.listarDocentes.and.returnValue(of(docentes));
    disponibilidad.consultarDocente.and.returnValue(of(bloques));

    await TestBed.configureTestingModule({
      declarations: [ConsultaComponent],
      imports: [FormsModule],
      providers: [
        { provide: DisponibilidadService, useValue: disponibilidad },
        { provide: ErrorHandlerService, useValue: errorHandler }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('se crea correctamente', () => {
    expect(component).toBeTruthy();
  });

  describe('carga de docentes', () => {
    it('ngOnInit() carga la lista de docentes', () => {
      expect(disponibilidad.listarDocentes).toHaveBeenCalledTimes(1);
      expect(component.docentes).toEqual(docentes);
      expect(component.cargandoDocentes).toBeFalse();
      expect(component.error).toBe('');
    });

    it('muestra los nombres de los docentes en pantalla', () => {
      const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
      expect(texto).toContain('Ana García');
      expect(texto).toContain('Luis Ríos');
    });

    it('ante un error muestra el mensaje y deja de cargar', () => {
      const fallo = new Error('500');
      disponibilidad.listarDocentes.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('No fue posible cargar los docentes.');

      component.cargarDocentes();

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible cargar los docentes.');
      expect(component.error).toBe('No fue posible cargar los docentes.');
      expect(component.cargandoDocentes).toBeFalse();
    });

    it('muestra el aviso cuando no hay docentes registrados', () => {
      disponibilidad.listarDocentes.and.returnValue(of([]));

      component.cargarDocentes();
      fixture.detectChanges();

      expect((fixture.nativeElement as HTMLElement).textContent).toContain('Todavía no hay docentes registrados.');
    });
  });

  describe('docentesFiltrados', () => {
    it('sin búsqueda devuelve todos los docentes', () => {
      component.busqueda = '';
      expect(component.docentesFiltrados.length).toBe(3);
    });

    it('filtra por nombre sin distinguir mayúsculas', () => {
      component.busqueda = 'ANA';
      expect(component.docentesFiltrados.map(d => d.id)).toEqual([1]);
    });

    it('filtra por apellido', () => {
      component.busqueda = 'ríos';
      expect(component.docentesFiltrados.map(d => d.id)).toEqual([2]);
    });

    it('filtra por nombre completo e ignora espacios en los extremos', () => {
      component.busqueda = '  luis ríos  ';
      expect(component.docentesFiltrados.map(d => d.id)).toEqual([2]);
    });

    it('devuelve vacío cuando nadie coincide y la vista lo informa', () => {
      component.busqueda = 'zzz';
      fixture.detectChanges();

      expect(component.docentesFiltrados).toEqual([]);
      expect((fixture.nativeElement as HTMLElement).textContent).toContain('Ningún docente coincide');
    });
  });

  describe('seleccionar()', () => {
    it('no hace nada cuando el id es undefined', () => {
      component.seleccionar(undefined);

      expect(disponibilidad.consultarDocente).not.toHaveBeenCalled();
      expect(component.idSeleccionado).toBeNull();
    });

    it('consulta la disponibilidad del docente y guarda los bloques', () => {
      component.seleccionar(1);

      expect(disponibilidad.consultarDocente).toHaveBeenCalledWith(1);
      expect(component.idSeleccionado).toBe(1);
      expect(component.bloques).toEqual(bloques);
      expect(component.cargandoBloques).toBeFalse();
      expect(component.docenteSeleccionado?.nombre).toBe('Ana');
    });

    it('limpia el error previo al seleccionar', () => {
      component.error = 'Error previo';
      component.seleccionar(1);
      expect(component.error).toBe('');
    });

    it('ante un error muestra el mensaje y deja de cargar', () => {
      const fallo = new Error('500');
      disponibilidad.consultarDocente.and.returnValue(throwError(() => fallo));
      errorHandler.extraerMensaje.and.returnValue('No fue posible consultar la disponibilidad.');

      component.seleccionar(2);

      expect(errorHandler.extraerMensaje).toHaveBeenCalledWith(fallo, 'No fue posible consultar la disponibilidad.');
      expect(component.error).toBe('No fue posible consultar la disponibilidad.');
      expect(component.cargandoBloques).toBeFalse();
      expect(component.bloques).toEqual([]);
    });

    it('docenteSeleccionado es undefined mientras no se elige a nadie', () => {
      expect(component.docenteSeleccionado).toBeUndefined();
    });
  });

  describe('calendario y estadísticas', () => {
    beforeEach(() => {
      component.seleccionar(1);
    });

    it('calendario tiene 7 columnas con los bloques agrupados por día', () => {
      const calendario = component.calendario;

      expect(calendario.length).toBe(7);
      expect(calendario[0].dia).toBe('LUNES');
      expect(calendario[0].bloques.length).toBe(2);
      expect(calendario[2].dia).toBe('MIERCOLES');
      expect(calendario[2].bloques.length).toBe(1);
      expect(calendario[1].bloques.length).toBe(0);
    });

    it('bloquesOrdenados ordena por día y luego por hora de inicio', () => {
      expect(component.bloquesOrdenados.map(b => b.id)).toEqual([2, 1, 3]);
    });

    it('bloquesOrdenados no modifica el arreglo original', () => {
      const ordenados = component.bloquesOrdenados;
      expect(ordenados).not.toBe(component.bloques);
      expect(component.bloques.map(b => b.id)).toEqual([1, 2, 3]);
    });

    it('diasConDisponibilidad cuenta solo los días con bloques', () => {
      expect(component.diasConDisponibilidad).toBe(2);
    });

    it('horasSemanales suma la duración de todos los bloques', () => {
      expect(component.horasSemanales).toBe('5 h 30 min');
    });

    it('horasSemanales muestra solo horas cuando no hay minutos sobrantes', () => {
      component.bloques = [bloques[0]];
      expect(component.horasSemanales).toBe('2 h');
    });

    it('horasSemanales es "0 h" sin bloques', () => {
      component.bloques = [];
      expect(component.horasSemanales).toBe('0 h');
    });

    it('horas cubre de 6 a 21 por defecto', () => {
      component.bloques = [];
      const horas = component.horas;

      expect(horas[0]).toBe(6);
      expect(horas[horas.length - 1]).toBe(21);
      expect(horas.length).toBe(16);
    });

    it('horas se amplía hacia arriba cuando un bloque empieza antes de las 6', () => {
      component.bloques = [{ id: 9, diaSemana: 'LUNES', horaInicio: '05:00:00', horaFin: '06:00:00' }];
      expect(component.horas[0]).toBe(5);
    });

    it('horas se amplía hacia abajo cuando un bloque termina después de las 22', () => {
      component.bloques = [{ id: 9, diaSemana: 'LUNES', horaInicio: '20:00:00', horaFin: '23:30:00' }];
      const horas = component.horas;
      expect(horas[horas.length - 1]).toBe(23);
    });

    it('arriba() calcula el desplazamiento desde la primera hora (44 px por hora)', () => {
      expect(component.arriba(bloques[0])).toBe(88); // 08:00 → 2 h después de las 06:00
      expect(component.arriba(bloques[1])).toBe(44); // 07:00 → 1 h después de las 06:00
    });

    it('alto() calcula la altura según la duración (44 px por hora)', () => {
      expect(component.alto(bloques[0])).toBe(88); // 2 h
      expect(component.alto(bloques[1])).toBe(66); // 1,5 h
    });
  });

  describe('utilidades', () => {
    it('horaCorta() recorta los segundos', () => {
      expect(component.horaCorta('08:00:00')).toBe('08:00');
      expect(component.horaCorta('14:30')).toBe('14:30');
    });

    it('etiquetaDia() devuelve el nombre del día o el valor recibido', () => {
      expect(component.etiquetaDia('MIERCOLES')).toBe('Miércoles');
      expect(component.etiquetaDia('XXX' as never)).toBe('XXX');
    });
  });
});
