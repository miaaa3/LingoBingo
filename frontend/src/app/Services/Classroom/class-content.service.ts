import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class ClassContentService {
  private baseUrl = environment.apiUrl + '/class-content';

  constructor(private http: HttpClient) {}

  assign(data: { classId: number; contentType: string; contentId: number }): Observable<any> {
    return this.http.post(`${this.baseUrl}/assign`, data);
  }

  unassign(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  forClass(classId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/class/${classId}`);
  }

  mine(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/mine`);
  }
}
