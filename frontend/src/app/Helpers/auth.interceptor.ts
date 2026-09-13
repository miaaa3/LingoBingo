import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HTTP_INTERCEPTORS
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocalService } from '../Services/Auth/local.service';
import { environment } from '../Environments/environment';
import { ToastrService } from 'ngx-toastr';



@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private tokenStorage: LocalService,
    ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenStorage.getData("userApiKey2");
      const isApiUrl = request.url.startsWith(environment.apiUrl);

      if (token && isApiUrl) {
          request = request.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
          });
          console.log(token)
      }
  

      return next.handle(request);
  }
}
