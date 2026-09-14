import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/app/Environments/environment';

export interface PracticeResultPayload { xp: number; correct: number; total: number; }

/** Saves a finished practice session so its XP feeds the dashboard. Never throws. */
@Injectable({ providedIn: 'root' })
export class PracticeService {
  private baseUrl = environment.apiUrl + '/practice';

  constructor(private http: HttpClient) {}

  complete(payload: PracticeResultPayload): Observable<any> {
    return this.http.post(`${this.baseUrl}/complete`, payload).pipe(catchError(() => of(null)));
  }
}
