import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class ClassroomService {
  private baseUrl = environment.apiUrl + '/classes';

  constructor(private http: HttpClient) {}

  createClass(data: { name: string; description: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  joinClass(joinCode: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/join`, { joinCode });
  }

  myClasses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/mine`);
  }

  members(classId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${classId}/members`);
  }
}
