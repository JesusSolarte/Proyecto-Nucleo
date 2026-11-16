import { DIAS_SEMANA, DiaSemana, diaCorto, etiquetaDia } from './dia-semana';

describe('dia-semana (modelo)', () => {

  it('DIAS_SEMANA contiene los 7 días en orden de lunes a domingo', () => {
    expect(DIAS_SEMANA.map(d => d.valor)).toEqual([
      'LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'
    ]);
  });

  it('cada día tiene etiqueta y abreviatura no vacías', () => {
    for (const dia of DIAS_SEMANA) {
      expect(dia.etiqueta.length).toBeGreaterThan(0);
      expect(dia.corto.length).toBe(3);
    }
  });

  it('etiquetaDia() devuelve el nombre completo con tilde', () => {
    expect(etiquetaDia('LUNES')).toBe('Lunes');
    expect(etiquetaDia('MIERCOLES')).toBe('Miércoles');
    expect(etiquetaDia('SABADO')).toBe('Sábado');
  });

  it('etiquetaDia() devuelve el mismo valor cuando el día no existe', () => {
    expect(etiquetaDia('FESTIVO' as DiaSemana)).toBe('FESTIVO');
  });

  it('diaCorto() devuelve la abreviatura', () => {
    expect(diaCorto('LUNES')).toBe('Lun');
    expect(diaCorto('MIERCOLES')).toBe('Mié');
    expect(diaCorto('DOMINGO')).toBe('Dom');
  });

  it('diaCorto() devuelve el mismo valor cuando el día no existe', () => {
    expect(diaCorto('FESTIVO' as DiaSemana)).toBe('FESTIVO');
  });
});
