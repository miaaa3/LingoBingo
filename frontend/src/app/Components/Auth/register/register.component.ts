import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ToastrService } from 'ngx-toastr';
import { AuthenticationService } from 'src/app/Services/Auth/authentication.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  isLoading = false;
  registerForm!: FormGroup;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private local: LocalService,
    private toastr: ToastrService,
    private authService: AuthenticationService,
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: [null, [Validators.required, Validators.email]],
      password: [null, [Validators.required, Validators.minLength(8)]],
      role: ['STUDENT', Validators.required],
    });
  }

  get passwordControl() {
    return this.registerForm.get('password');
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.authService.register(this.registerForm.value).subscribe(
      res => {
        this.local.saveData('userApiKey2', res['access_token']);
        if (res['id'] != null) {
          this.local.saveData('userid', res['id'].toString());
        }
        if (res['role'] != null) {
          this.local.saveData('role', res['role']);
        }
        this.toastr.success('Sign up successful.', 'Success');
        this.router.navigate(['/Home']);
        this.isLoading = false;
      },
      err => {
        this.toastr.error(err?.error || 'Error during sign up.', 'Error');
        this.isLoading = false;
      }
    );
  }
}
