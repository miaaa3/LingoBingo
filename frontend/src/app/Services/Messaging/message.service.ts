import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private baseUrl = environment.apiUrl + '/messages';

  constructor(private http: HttpClient) {}

  contacts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/contacts`);
  }

  threads(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/threads`);
  }

  thread(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/with/${userId}`);
  }

  send(recipientId: number, content: string): Observable<any> {
    return this.http.post(this.baseUrl, { recipientId, content });
  }

  unreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/unread-count`);
  }
}
