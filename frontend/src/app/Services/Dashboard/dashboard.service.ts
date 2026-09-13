import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}
  me(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/dashboard/me`);
  }
}
