import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

export interface BingoWord { term: string; definition: string; }

/** A batch of grids; each grid is a flat row-major list of word indexes, -1 = FREE. */
export interface BingoBatch {
  setTitle: string;
  size: number;
  words: BingoWord[];
  grids: number[][];
  uniqueBy: 'words' | 'layout';
}

export interface BingoCheck { won: boolean; lines: string[]; }

export const FREE = -1;

/**
 * Talks to /api/bingo. The server builds the all-different grids and referees
 * wins (BingoService, unit-tested); the browser just keeps the batch.
 */
@Injectable({ providedIn: 'root' })
export class BingoService {
  private baseUrl = environment.apiUrl + '/bingo';

  constructor(private http: HttpClient) {}

  generate(setId: number, size: number, count: number, freeCenter: boolean): Observable<BingoBatch> {
    return this.http.post<BingoBatch>(`${this.baseUrl}/generate`, { setId, size, count, freeCenter });
  }

  check(grid: number[], called: number[]): Observable<BingoCheck> {
    return this.http.post<BingoCheck>(`${this.baseUrl}/check`, { grid, called });
  }
}
