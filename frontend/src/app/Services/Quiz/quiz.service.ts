import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';
import { Quiz } from 'src/app/Models/quiz/quiz.model';

@Injectable({
  providedIn: 'root'
})
export class QuizService {

  private baseUrl = environment.apiUrl + '/quizzes';

  constructor(private http: HttpClient) {}

  /**
   * Get all quizzes.
   * Sends GET request to '/all'.
   */
  getAllQuizzes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/all`);
  }

  /**
   * Get quiz by ID.
   * Sends GET request to '/getQuizById/{id}'.
   * @param id ID of the quiz.
   */
  getQuizById(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.baseUrl}/getQuizById/${id}`);
  }

  /**
   * Add a new quiz.
   * Sends POST request to '/add' with quiz data.
   * @param quiz Quiz object to add.
   */
  addQuiz(quiz: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, quiz);
  }

  /**
   * Update an existing quiz.
   * Sends PUT request to '/{id}/update' with updated quiz data.
   * @param id ID of the quiz.
   * @param updatedQuiz Updated quiz object.
   */
  updateQuiz(id: number, updatedQuiz: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/update`, updatedQuiz);
  }

  /**
   * Delete a quiz by ID.
   * Sends DELETE request to '/{id}/delete'.
   * @param id ID of the quiz.
   */
  deleteQuiz(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/delete`);
  }

  /**
   * Set quiz visibility (public/private).
   * Sends PUT request to '/{id}/visibility' with query param 'isPublic'.
   * @param id ID of the quiz.
   * @param isPublic Boolean flag for visibility.
   */
  setQuizVisibility(id: number, isPublic: boolean): Observable<any> {
    const params = new HttpParams().set('isPublic', String(isPublic));
    return this.http.put(`${this.baseUrl}/${id}/visibility`, null, { params });
  }

  /**
   * Search quizzes with optional filters: name, isPublic, createdById.
   * Sends GET request to '/search' with query params.
   * @param name Optional quiz name filter.
   * @param isPublic Optional visibility filter.
   * @param createdById Optional creator user ID.
   */

searchQuizzesSeparate(name?: string, isPublic?: boolean): Observable<{ ownQuizzes: any[]; publicQuizzes: any[] }> {
  let params = new HttpParams();

  if (name) params = params.set('name', name);
  if (isPublic !== undefined && isPublic !== null) params = params.set('isPublic', String(isPublic));

  return this.http.get<{ ownQuizzes: any[]; publicQuizzes: any[] }>(`${this.baseUrl}/search-separate`, { params });
}



  /**
   * Get the last created quiz.
   * Sends GET request to '/last'.
   */
  getLastQuiz(): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.baseUrl}/last`);
  }

  getVisibleQuizzes():  Observable<{ ownQuizzes: any[]; publicQuizzes: any[] }> {
  return this.http.get<{ ownQuizzes: any[]; publicQuizzes: any[] }>(`${this.baseUrl}/visible`);
}
}
