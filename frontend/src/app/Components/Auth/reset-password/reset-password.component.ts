import { HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { RestApiService } from 'src/app/Services/Auth/rest-api.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent {
  resetForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private api: RestApiService,
    private local: LocalService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.resetForm = this.fb.group({
      old_password: ['', [Validators.required, Validators.minLength(8)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirm_password: ['', [Validators.required, Validators.minLength(8)]],
      acceptTerms: [false, Validators.requiredTrue],
    }, { validators: [this.passwordMatchValidator, Validators.required] });  // Use "validators" instead of "Validator"
  }

  ngOnInit(): void {}


  setNewPassword(): void {
    this.isLoading = true;
    this.api.httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Accept: 'application/json',
        Authorization: 'Bearer ' + this.local.getData("userApiKey")
      })
    };

    let data = {
      "old_password" : this.resetForm.get('old_password')?.value,
      "new_password" : this.resetForm.get('password')?.value,
    };

    this.api.resetPassword(data).subscribe(
      res => {
        this.isLoading = false;
        this.toastr.success('Password changed successfully.', 'Success');
        setTimeout(() => {
          this.router.navigate(['/Home']);
        }, 200);
      },
      err => {
        this.isLoading = false;
        this.toastr.error('Error while changing password.', 'Error');
      }
    );
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirm_password = control.get('confirm_password')?.value;

    return password === confirm_password ? null : { passwordMismatch: true };
  }
}
