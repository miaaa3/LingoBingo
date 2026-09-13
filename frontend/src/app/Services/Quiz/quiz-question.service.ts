import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';
import { Question } from 'src/app/Models/quiz/question.model';
import { CheckAnswerResponse } from 'src/app/Models/quiz/CheckAnswerResponse';
import { UserAnswerRequest } from 'src/app/Models/quiz/userAnswerRequest.model';

@Injectable({
  providedIn: 'root'
})
export class QuizQuestionService {
  private apiUrl = environment.apiUrl + '/quizzes';

  constructor(private http: HttpClient) { }

  // Create question without image
  createQuestion(question: Question): Observable<Question> {
    return this.http.post<Question>(this.apiUrl, question);
  }

  // Create question with image upload
  createQuestionWithImage(question: Question, imageFile: File): Observable<Question> {
    const formData = new FormData();
    formData.append('question', new Blob([JSON.stringify(question)], { type: 'application/json' }));
    formData.append('image', imageFile);

    return this.http.post<Question>(`${this.apiUrl}/with-image`, formData);
  }

  // Get all questions
  getAllQuestions(): Observable<Question[]> {
    return this.http.get<Question[]>(this.apiUrl);
  }

  // Get question by ID
  getQuestionById(questionId: number): Observable<Question> {
    return this.http.get<Question>(`${this.apiUrl}/${questionId}`);
  }

  // Get question image by question ID
  getQuestionImage(questionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${questionId}/image`, { responseType: 'blob' });
  }

  // Update question without image
  updateQuestion(questionId: number, question: Question): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/${questionId}`, question);
  }

  // Update question image only
  updateQuestionImage(questionId: number, imageFile: File): Observable<Question> {
    const formData = new FormData();
    formData.append('image', imageFile);

    return this.http.put<Question>(`${this.apiUrl}/${questionId}/image`, formData);
  }

  // Delete question
  deleteQuestion(questionId: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${questionId}`);
  }

  // Check answer correctness
  checkAnswer(userAnswerRequest: UserAnswerRequest): Observable<CheckAnswerResponse> {
  return this.http.post<CheckAnswerResponse>(`${this.apiUrl}/check-answer`, userAnswerRequest);
}

}
