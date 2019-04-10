import { By } from '@angular/platform-browser';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { LoginComponent, LoginDialogParameter } from './login.component';
import { DebugElement } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartsModule } from 'ng2-charts';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';

describe('LoginComponent', () => {

  const param1: LoginDialogParameter = require('src/assets/test-data/component/login/login-dialog-parameter1.json');

  let authService: jasmine.SpyObj<AuthenticationService>;
  let errorHandler: jasmine.SpyObj<DefaultErrorHandlerService>;
  let dialog: jasmine.SpyObj<DialogService>;
  let router: jasmine.SpyObj<Router>;

  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let debugElement: DebugElement;

  const dialogSubject = new Subject<LoginDialogParameter>();

  beforeEach(async(() => {

    const authServiceSpy = jasmine.createSpyObj<AuthenticationService>(['login']);
    const errorHandlerSpy = jasmine.createSpyObj<DefaultErrorHandlerService>(['handleSession']);
    const dialogSpy = jasmine.createSpyObj<DialogService>(['loading', 'login']);
    const routerSpy = jasmine.createSpyObj<Router>(['navigate']);
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        { provide: AuthenticationService, useValue: authServiceSpy },
        { provide: DefaultErrorHandlerService, useValue: errorHandlerSpy },
        { provide: DialogService, useValue: dialogSpy },
        { provide: Router, useValue: routerSpy },
      ],
      imports: [
        CommonModule,
        ChartsModule,
        FormsModule,
      ]
    }).compileComponents();
  }));

  beforeEach(() => {

    authService = TestBed.get(AuthenticationService);
    errorHandler = TestBed.get(DefaultErrorHandlerService);
    dialog = TestBed.get(DialogService);
    router = TestBed.get(Router);
    dialog.login$ = dialogSubject.asObservable();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
    dialogSubject.next(param1);
    fixture.detectChanges();
  });

  xit('ログイン成功', () => {

    debugElement.query(By.css('#username')).nativeElement.value = 'username';
    debugElement.query(By.css('#password')).nativeElement.value = 'password';
    fixture.detectChanges();
  });
});
