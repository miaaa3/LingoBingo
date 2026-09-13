import { Component, ElementRef, OnInit, Renderer2 } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { RestApiService } from '../../../Services/Auth/rest-api.service';

import { HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ToastrService } from 'ngx-toastr';



@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit{
  isLoading = false;
  registerForm!: FormGroup;

  showPassword: boolean = false;

  constructor(
    private api: RestApiService,
    private fb: FormBuilder,
    private router: Router,
    private local:LocalService,
    private toastr: ToastrService,

    ) {}

  ngOnInit() {
    this.registerForm = this.fb.group({
      username: ['', Validators.required, Validators.minLength(4)],
      email: [null, <any>[Validators.required, Validators.email]],
      password: [null, <any>[Validators.required, Validators.minLength(8)]],
    },
    { validators: [Validators.required] }
    );
  }



  get passwordControl() {
    return this.registerForm.get('password');
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }
  async register() {
    this.isLoading = true;
    this.api.httpOptions = {
            headers: new HttpHeaders({
              'Content-Type': 'application/json',
              Accept: 'application/json',
            })
          };
    this.api.register(this.registerForm.value).subscribe(
      res => {
        this.api.token = res['access_token'];
        this.local.saveData("userApiKey",this.api.token!)
        
        this.api.httpOptions = {
          headers: new HttpHeaders({
            'Content-Type': 'application/json',
            Accept: 'application/json',
            Authorization: 'Bearer ' + this.api.token
          })
        };
        this.api.user = res['user'];
        this.toastr.success('Sign up successfully.', 'Success');
        this.router.navigate(['/Home']);
        setTimeout(() => {
          this.isLoading = false;
        }, 2000);
    
      },
      err=>{
        this.toastr.error('Error during sign up.', 'Error');

          this.isLoading = false;
      }
    );
  }
}
