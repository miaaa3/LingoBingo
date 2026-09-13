import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from 'src/app/Environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FlashcardService {

  private baseUrl = environment.apiUrl+'/flashcards';

  constructor(private http: HttpClient) {}

  getAllFlashcards(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/all`);
  }

  getFlashcardById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  addFlashcard(flashcard: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, flashcard);
  }

  updateFlashcard(id: number, updatedFlashcard: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/update`, updatedFlashcard);
  }

  deleteFlashcard(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/delete`);
  }
}
