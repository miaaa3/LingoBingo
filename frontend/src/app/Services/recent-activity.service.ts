import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../Environments/environment';
import {  RecentActivityDTO } from '../Models/recent-activity.DTO';

@Injectable({
  providedIn: 'root'
})
export class RecentActivityService {
  private apiUrl = environment.apiUrl+'/recent-activities';

  constructor(private http: HttpClient) {}

  saveRecentActivity(recentActivityDTO: RecentActivityDTO): Observable<any> {
    const url = `${this.apiUrl}/save`;
    return this.http.post(url, recentActivityDTO);
  }

  getRecentActivitiesForUser(): Observable<any> {
    const url = `${this.apiUrl}`;
    return this.http.get(url);
  }

  getRecentActivitiesForQuiz(id: number): Observable<any> {
    const url = `${this.apiUrl}/quiz/${id}`;
    return this.http.get(url);
  }

  getRecentActivitiesForFlashcardSet(flashcardSetId: number): Observable<any> {
    const url = `${this.apiUrl}/flashcard-set/${flashcardSetId}`;
    return this.http.get(url);
  }
}
