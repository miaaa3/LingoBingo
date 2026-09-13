import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocalService } from '../Services/Auth/local.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private localService: LocalService,
    private router: Router,
    private toastr: ToastrService)
      {}

  canActivate(): boolean {
    const token = this.localService.getData('userApiKey2');

    if (token) {
      // Token exists, allow navigation
      return true;
    } else {
      this.toastr.info('You are not authenticated. Please log in to access this resource.', '');
      this.router.navigate(['/Home']);
      return false;
    }
  }
}
