export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO';

export const DIAS_SEMANA: { valor: DiaSemana; etiqueta: string; corto: string }[] = [
  { valor: 'LUNES', etiqueta: 'Lunes', corto: 'Lun' },
  { valor: 'MARTES', etiqueta: 'Martes', corto: 'Mar' },
  { valor: 'MIERCOLES', etiqueta: 'Miércoles', corto: 'Mié' },
  { valor: 'JUEVES', etiqueta: 'Jueves', corto: 'Jue' },
  { valor: 'VIERNES', etiqueta: 'Viernes', corto: 'Vie' },
  { valor: 'SABADO', etiqueta: 'Sábado', corto: 'Sáb' },
  { valor: 'DOMINGO', etiqueta: 'Domingo', corto: 'Dom' }
];

export function etiquetaDia(dia: DiaSemana): string {
  const encontrado = DIAS_SEMANA.find(d => d.valor === dia);
  return encontrado ? encontrado.etiqueta : dia;
}

export function diaCorto(dia: DiaSemana): string {
  const encontrado = DIAS_SEMANA.find(d => d.valor === dia);
  return encontrado ? encontrado.corto : dia;
}
