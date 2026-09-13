import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/Environments/environment';
import { LocalService } from './local.service';

  const apiUrl = 'http://127.0.0.1:8000/api';
  


@Injectable({
  providedIn: 'root'
})
export class RestApiService {

  public httpOptions: any;
  public token?: string;
  public loggued: any = false;
  public user: any;
  private baseUrl = environment.apiUrl+'/quizzes';


  constructor(
    private http: HttpClient,
    private tokenStorage: LocalService,
    ) { }

  login(data:any): Observable<any> {
    const url = `${apiUrl}/login`;
    return this.http.post(url, data).pipe();
  }

  register(data:any): Observable<any> {
    const url = `${apiUrl}/register`;
    return this.http.post(url, data).pipe();
  }
  profile(): Observable<any> {
    const url = `${apiUrl}/profile`;
    return this.http.get(url, this.httpOptions).pipe();
  }
  resetPassword(data:any): Observable<any> {
    const url = `${apiUrl}/change-password`;
    return this.http.post(url, data,this.httpOptions).pipe();
  }

  sendPasswordResetEmail(data:any): Observable<any> {
    const url = `${apiUrl}/send-reset-password-link`;
    return this.http.post(url, data).pipe();
  }

  addQuiz(data:any){
    const url = `${this.baseUrl}/add`;
    const httpHeader = {
      headers: new HttpHeaders({
        Authorization: 'Bearer '+this.tokenStorage.getData("userApiKey2"),
      })
    }
    console.log(httpHeader)
    return this.http.post(url, data,httpHeader).pipe();
  }
  getLastId(){
    const url = `${this.baseUrl}/last`;
    return this.http.get(url).pipe();
  }
}
