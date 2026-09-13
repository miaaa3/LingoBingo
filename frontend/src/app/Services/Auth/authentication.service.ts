import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private baseUrl = environment.apiUrl+'/authentication';

  constructor(private http: HttpClient) {}

  register(registerRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, registerRequest);
  }

  login(authenticationRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, authenticationRequest).pipe();
  }

  getAuthenticatedUser(): Observable<any> {
    return this.http.get(`${this.baseUrl}/user`);
  }
}
