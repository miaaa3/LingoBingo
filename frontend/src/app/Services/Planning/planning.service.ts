import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class PlanningService {
  private baseUrl = environment.apiUrl + '/planning';

  constructor(private http: HttpClient) {}

  create(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  mine(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/mine`);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
