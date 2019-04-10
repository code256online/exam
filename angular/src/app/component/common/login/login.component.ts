import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  params: LoginDialogParameter;
  subscription: Subscription;

  errorMessage: string;

  constructor(
    private authService: AuthenticationService,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.subscription = this.dialog.login$.subscribe(x => this.params = Object.assign({}, x));
    this.loginForm = new FormGroup({
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required),
    });
  }

  login(): void {
    this.dialog.loading(true);
    this.authService.login({
      username: this.loginForm.controls['username'].value,
      password: this.loginForm.controls['password'].value,
    }).then(x => {
      this.params.show = false;
      if (this.params.callback) {
        this.params.callback();
      } else if (this.params.redirectTo) {
        this.router.navigate([this.params.redirectTo]);
      }
    }).catch(error => {
      if (error.error.status === 400) {
        this.params.show = false;
        this.errorHandler.handleSession(error);
      } else {
        this.errorMessage = error.error.message;
      }
    }).finally(() => this.dialog.loading(false));
  }

  closeError(): void {
    this.errorMessage = undefined;
  }

  close(event: any): void {
    event.stopPropagation();
    this.params.show = false;
  }

  cancelEvent(event: any) {
    event.stopPropagation();
  }
}

export class LoginDialogParameter {

  show: boolean;
  redirectTo?: String;
  callback?: () => void;
}
