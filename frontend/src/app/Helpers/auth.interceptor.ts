import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocalService } from '../Services/Auth/local.service';
import { environment } from '../Environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // Prevents a burst of failed requests from each triggering a redirect/toast.
  private redirecting = false;

  // Router/Toastr are pulled lazily to avoid a cyclic dependency
  // (Router -> HttpClient -> this interceptor -> Router).
  constructor(private tokenStorage: LocalService, private injector: Injector) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenStorage.getData('userApiKey2');
    const isApiUrl = request.url.startsWith(environment.apiUrl);
    const isAuthCall = request.url.includes('/authentication/');
    // A 403 on these is NOT "session expired", so it must never sign the user
    // out: /results & /gradebook are role-restricted, and the *-count polls run
    // in the background (a transient 403 there right after login must not log
    // the user straight back out).
    const skipLogout = /\/results|\/gradebook|\/unread-count/.test(request.url);

    if (token && isApiUrl) {
      request = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(request).pipe(
      catchError((err: HttpErrorResponse) => {
        if (token && isApiUrl && !isAuthCall && !skipLogout &&
            (err.status === 401 || err.status === 403)) {
          this.handleSessionExpired();
        }
        return throwError(() => err);
      })
    );
  }

  /** The token is gone or expired: clear it and send the user to sign in again. */
  private handleSessionExpired(): void {
    if (this.redirecting) { return; }
    this.redirecting = true;

    const router = this.injector.get(Router);
    const toastr = this.injector.get(ToastrService);

    this.tokenStorage.clearData();
    try { toastr.info('Your session expired — please sign in again.'); } catch { /* toast is best-effort */ }

    router.navigate(['/Login']).finally(() => {
      setTimeout(() => (this.redirecting = false), 1500);
    });
  }
}
