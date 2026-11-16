import { ThemeService } from './theme.service';

describe('ThemeService', () => {

  beforeEach(() => {
    localStorage.clear();
    document.body.classList.remove('dark-mode');
  });

  afterEach(() => {
    localStorage.clear();
    document.body.classList.remove('dark-mode');
  });

  it('inicia en modo claro cuando no hay preferencia guardada', () => {
    const servicio = new ThemeService();

    expect(servicio.isDark()).toBeFalse();
    expect(document.body.classList.contains('dark-mode')).toBeFalse();
  });

  it('inicia en modo oscuro cuando la preferencia guardada es "oscuro"', () => {
    localStorage.setItem('tema', 'oscuro');

    const servicio = new ThemeService();

    expect(servicio.isDark()).toBeTrue();
    expect(document.body.classList.contains('dark-mode')).toBeTrue();
  });

  it('inicia en modo claro cuando la preferencia guardada es "claro"', () => {
    localStorage.setItem('tema', 'claro');

    expect(new ThemeService().isDark()).toBeFalse();
  });

  it('toggle() pasa de claro a oscuro, lo guarda y aplica la clase al body', () => {
    const servicio = new ThemeService();

    servicio.toggle();

    expect(servicio.isDark()).toBeTrue();
    expect(localStorage.getItem('tema')).toBe('oscuro');
    expect(document.body.classList.contains('dark-mode')).toBeTrue();
  });

  it('toggle() dos veces vuelve a modo claro y quita la clase del body', () => {
    const servicio = new ThemeService();

    servicio.toggle();
    servicio.toggle();

    expect(servicio.isDark()).toBeFalse();
    expect(localStorage.getItem('tema')).toBe('claro');
    expect(document.body.classList.contains('dark-mode')).toBeFalse();
  });
});
