import { HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthenticationService } from 'src/app/Services/Auth/authentication.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { RestApiService } from 'src/app/Services/Auth/rest-api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  
  loginForm!: FormGroup;
  showPassword: boolean = false;  // For toggling password visibility
  isLoading = false;

  constructor(
    private api: RestApiService,
    private fb: FormBuilder,
    private router: Router,
    private local: LocalService,
    private toastr: ToastrService,
    private authService: AuthenticationService
  ) {}

  ngOnInit(): void {
    // Initialize the form with validators
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      password: [null, [Validators.required, Validators.minLength(8)]],
    });
  }

  // Getter for password form control
  get passwordControl() {
    return this.loginForm.get('password');
  }
  
  login() {
    this.isLoading = true;
  
    this.api.httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Accept: 'application/json',
      })
    };
  
    console.log(this.loginForm.value);
  
    this.authService.login(this.loginForm.value).subscribe(
      res => {
        console.log(res['access_token']);
        console.log(res['id']);

  
        // Save token locally
        this.local.saveData("userApiKey2", res['access_token'] );
        this.local.saveData("userid", res['id'].toString());
  
        // Save user data if it's included in response
        this.api.user = res['user'];
        
        
        // Success toast
        this.toastr.success('Logged in successfully.', 'Success');
  
        // Navigate to Home
        this.router.navigate(['/Home']).then(() => {
          this.ngOnInit();  // Refresh component state if needed
        });
  
        // Stop loading after 2 sec (or directly after navigation if you prefer)
        setTimeout(() => {
          this.isLoading = false;
        }, 2000);
      },
      err => {
        console.error("Login error:", err);
        this.toastr.error('Error during login.', 'Error');
        this.isLoading = false;
      }
    );
  }
  

  // Toggle password visibility function
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }
}
