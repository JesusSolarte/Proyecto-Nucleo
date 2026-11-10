import { Injectable } from '@angular/core';

const CLAVE_TEMA = 'tema';

/**
 * Gestiona el tema claro/oscuro de la aplicación y lo persiste.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {

  private oscuro: boolean;

  constructor() {
    this.oscuro = localStorage.getItem(CLAVE_TEMA) === 'oscuro';
    this.aplicar();
  }

  isDark(): boolean {
    return this.oscuro;
  }

  toggle(): void {
    this.oscuro = !this.oscuro;
    localStorage.setItem(CLAVE_TEMA, this.oscuro ? 'oscuro' : 'claro');
    this.aplicar();
  }

  private aplicar(): void {
    document.body.classList.toggle('dark-mode', this.oscuro);
  }
}
