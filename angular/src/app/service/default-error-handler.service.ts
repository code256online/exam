import { errorModalParams } from '../../environments/constants';
import { Injectable } from '@angular/core';
import { DialogService } from './dialog.service';

@Injectable({
  providedIn: 'root'
})
export class DefaultErrorHandlerService {

  constructor(
    private dialog: DialogService,
  ) { }

  handle(error: any): void {

    console.log(error);
    if (error.error && error.error.status === 400) {
      this.handleSession(error);
    } else {
      const dialogParams = errorModalParams.default;
      dialogParams.trace = JSON.stringify(error, null, 2);
      this.dialog.loading(false);
      this.dialog.error(dialogParams);
    }
  }

  handleSession(error: any): void {

    const dialogParams = errorModalParams.timeout;
    this.dialog.loading(false);
    this.dialog.error(dialogParams);
  }
}
