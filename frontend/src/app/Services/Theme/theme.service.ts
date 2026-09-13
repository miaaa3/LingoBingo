import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Theme = 'light' | 'dark';
const STORAGE_KEY = 'lb-theme';

/**
 * Owns the app's light/dark theme: reads the saved choice (falling back to the
 * OS preference), toggles the `.dark` class on <html>, and persists changes.
 * Kept deliberately small and self-contained so no component depends on how
 * theming is stored or applied.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly _theme = new BehaviorSubject<Theme>('light');
  readonly theme$ = this._theme.asObservable();

  /** Resolve the initial theme and apply it. Called once at app startup. */
  init(): void {
    this.set(this.read() ?? this.systemPreference(), false);
  }

  toggle(): void {
    this.set(this._theme.value === 'dark' ? 'light' : 'dark');
  }

  get current(): Theme {
    return this._theme.value;
  }

  private set(theme: Theme, persist = true): void {
    this._theme.next(theme);
    document.documentElement.classList.toggle('dark', theme === 'dark');
    if (persist) {
      try { localStorage.setItem(STORAGE_KEY, theme); } catch { /* storage blocked */ }
    }
  }

  private read(): Theme | null {
    try {
      const v = localStorage.getItem(STORAGE_KEY);
      return v === 'dark' || v === 'light' ? v : null;
    } catch { return null; }
  }

  private systemPreference(): Theme {
    try {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    } catch { return 'light'; }
  }
}
