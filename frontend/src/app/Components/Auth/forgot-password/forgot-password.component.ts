import { HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Route, Router } from '@angular/router';
import { RestApiService } from 'src/app/Services/Auth/rest-api.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent  implements OnInit{
  forgotPasswordForm!: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private api: RestApiService,
    private router:Router,
    private toastr:ToastrService
    ) {}
  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  sendPasswordResetEmail(){
    this.isLoading = true;
    this.api.httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Accept: 'application/json',
      })
    };

    console.warn("from Toeas :"+this.forgotPasswordForm.value)
    this.api.sendPasswordResetEmail(this.forgotPasswordForm.value).subscribe(
      res => {
        this.isLoading = false;
          this.toastr.success('Password reset email sent successfully.', 'Success');
        setTimeout(() => {
          this.router.navigate(['/Login']);
        }, 2000);
      },
      err =>{
        console.warn(err)
          this.isLoading = false;
          this.toastr.error('Error sending password reset email.', 'Error');
        
      
      }
    );
}

}
