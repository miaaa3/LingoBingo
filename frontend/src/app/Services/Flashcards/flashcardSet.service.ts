import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';
import { LocalService } from '../Auth/local.service';

@Injectable({
  providedIn: 'root'
})
export class FlashcardSetService {
  private baseUrl = environment.apiUrl + '/flashcard-sets';

  constructor(
    private http: HttpClient,
    private tokenStorage: LocalService,
  ) {}

  /**
   * Fetch all flashcard sets.
   * Sends GET request to '/all' endpoint.
   * @returns Observable array of flashcard sets.
   */
  getAllFlashcardSets(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/all`);
  }

  /**
   * Fetch a single flashcard set by its ID.
   * Sends GET request to '/{id}' endpoint.
   * @param id - FlashcardSet ID
   * @returns Observable of the flashcard set object.
   */
  getFlashcardSetById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  /**
   * Add a new flashcard set.
   * Sends POST request to '/add' endpoint with flashcardSet data.
   * @param flashcardSet - The flashcard set object to create.
   * @returns Observable of the created flashcard set.
   */
  addFlashcardSet(flashcardSet: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, flashcardSet);
  }

  /**
   * Update an existing flashcard set.
   * Sends PUT request to '/{id}/update' endpoint with updated data.
   * @param id - ID of the flashcard set to update.
   * @param updatedFlashcardSet - Updated flashcard set object.
   * @returns Observable of the updated flashcard set.
   */
  updateFlashcardSet(id: number, updatedFlashcardSet: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/update`, updatedFlashcardSet);
  }

  /**
   * Delete a flashcard set by ID.
   * Sends DELETE request to '/{id}/delete' endpoint.
   * @param id - ID of the flashcard set to delete.
   * @returns Observable of the deletion result.
   */
  deleteFlashcardSet(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/delete`);
  }

  /**
   * Set the visibility (public or private) of a flashcard set.
   * Sends PUT request to '/{id}/visibility' with 'isPublic' as query parameter.
   * @param id - ID of the flashcard set.
   * @param isPublic - Boolean flag for visibility.
   * @returns Observable of the updated flashcard set.
   */
  setFlashcardSetVisibility(id: number, isPublic: boolean): Observable<any> {
    const params = new HttpParams().set('isPublic', String(isPublic));
    return this.http.put(`${this.baseUrl}/${id}/visibility`, null, { params });
  }

  /**
   * Search flashcard sets with optional filters: name, description, and visibility.
   * Sends GET request to '/search' endpoint with query parameters.
   * @param name - Optional name substring to search.
   * @param description - Optional description substring to search.
   * @param isPublic - Optional visibility filter.
   * @returns Observable array of matching flashcard sets.
   */

searchFlashcardSetsSeparate(
  name?: string,
  description?: string,
  isPublic?: boolean
): Observable<{ ownSets: any[]; publicSets: any[] }> {
  let params = new HttpParams();

  if (name) params = params.set('name', name);
  if (description) params = params.set('description', description);
  if (isPublic !== undefined && isPublic !== null) params = params.set('isPublic', String(isPublic));

  return this.http.get<{ ownSets: any[]; publicSets: any[] }>(`${this.baseUrl}/search-separate`, { params });
}

  getVisibleFlashcardSets():Observable<{ ownSets: any[]; publicSets: any[] }> {
  return this.http.get<{ ownSets: any[]; publicSets: any[] }>(`${this.baseUrl}/visible`);
}

}
