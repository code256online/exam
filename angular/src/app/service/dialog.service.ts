import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter, map, skip } from 'rxjs/operators';
import { ErrorDialogParameter } from '../component/common/error-dialog/error-dialog.component';
import { LoginDialogParameter } from '../component/common/login/login.component';
import { ModalDialogParameter } from '../component/common/modal-dialog/modal-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  private errorDialogSource = new Subject<ErrorDialogParameter>();
  private modalDialogSource = new Subject<ModalDialogParameter>();
  private loadingSource = new Subject<boolean>();
  private loginSource = new Subject<LoginDialogParameter>();

  errorDialog$ = this.errorDialogSource.asObservable();
  modalDialog$ = this.modalDialogSource.asObservable();
  loading$ = this.loadingSource.asObservable();
  login$ = this.loginSource.asObservable();

  constructor() { }

  error(errorDialogParams: ErrorDialogParameter): void {
    this.errorDialogSource.next(errorDialogParams);
  }

  modal(modalDialogParams: ModalDialogParameter): Observable<boolean> {
    this.modalDialogSource.next(modalDialogParams);
    return this.modalDialog$.pipe(
      filter(x => !x.show),
      map(x => x.ok));
  }

  loading(isLoading: boolean): void {
    this.loadingSource.next(isLoading);
  }

  login(loginDialogParameter: LoginDialogParameter): void {
    this.loginSource.next(loginDialogParameter);
  }
}
