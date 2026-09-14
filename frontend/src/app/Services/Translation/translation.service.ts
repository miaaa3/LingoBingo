import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from 'src/app/Environments/environment';

export interface TranslationResult { translation: string; ok: boolean; }

/** Supported UI languages for translation decks. */
export const LANGUAGES: { code: string; name: string; flag: string }[] = [
  { code: 'en', name: 'English', flag: '🇬🇧' },
  { code: 'fr', name: 'French',  flag: '🇫🇷' },
  { code: 'es', name: 'Spanish', flag: '🇪🇸' },
  { code: 'de', name: 'German',  flag: '🇩🇪' },
  { code: 'it', name: 'Italian', flag: '🇮🇹' },
  { code: 'ar', name: 'Arabic',  flag: '🇲🇦' },
  { code: 'pt', name: 'Portuguese', flag: '🇵🇹' },
  { code: 'nl', name: 'Dutch',   flag: '🇳🇱' },
];

/** Wraps the backend translation proxy. Never throws — returns ok:false instead. */
@Injectable({ providedIn: 'root' })
export class TranslationService {
  private baseUrl = environment.apiUrl + '/translate';

  constructor(private http: HttpClient) {}

  translate(q: string, from: string, to: string): Observable<TranslationResult> {
    const text = (q || '').trim();
    if (!text) { return of({ translation: '', ok: false }); }
    const params = new HttpParams().set('q', text).set('from', from).set('to', to);
    return this.http.get<any>(this.baseUrl, { params }).pipe(
      map(r => ({ translation: r?.translation || '', ok: !!r?.ok })),
      catchError(() => of({ translation: '', ok: false })),
    );
  }

  flag(code: string): string {
    return LANGUAGES.find(l => l.code === code)?.flag || '';
  }
  name(code: string): string {
    return LANGUAGES.find(l => l.code === code)?.name || code;
  }
}
