import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

export interface Profile {
  id: number; name: string; username: string; email: string; role: string; createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private baseUrl = environment.apiUrl + '/users';
  constructor(private http: HttpClient) {}

  me(): Observable<Profile> { return this.http.get<Profile>(`${this.baseUrl}/me`); }
  update(data: { name?: string; username?: string }): Observable<Profile> {
    return this.http.put<Profile>(`${this.baseUrl}/me`, data);
  }
}
