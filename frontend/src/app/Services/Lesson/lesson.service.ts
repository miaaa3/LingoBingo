import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class LessonService {
  private baseUrl = environment.apiUrl + '/lessons';

  constructor(private http: HttpClient) {}

  getPublicCourses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/public`);
  }

  getMyCourses(userId: number | string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user/${userId}`);
  }

  getCourse(id: number | string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  createCourse(form: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, form);
  }

  deleteCourse(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/delete`, { responseType: 'text' });
  }

  toggleVisibility(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/visibility/toggle`, null);
  }

  sectionImageUrl(sectionId: number): string {
    return `${this.baseUrl}/sections/${sectionId}/image`;
  }
}
