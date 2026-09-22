import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private baseUrl = environment.apiUrl + '/notifications';

  constructor(private http: HttpClient) {}

  /** Current user's latest notifications. */
  list(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  unreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/unread-count`);
  }

  markRead(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read`, null);
  }

  markAllRead(): Observable<any> {
    return this.http.put(`${this.baseUrl}/read-all`, null);
  }
}
