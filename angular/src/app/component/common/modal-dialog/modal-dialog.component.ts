import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { DialogService } from 'src/app/service/dialog.service';

@Component({
  selector: 'app-modal-dialog',
  templateUrl: './modal-dialog.component.html',
  styleUrls: ['./modal-dialog.component.scss']
})
export class ModalDialogComponent implements OnInit, OnDestroy {

  subscription: Subscription;
  params: ModalDialogParameter;

  constructor(
    private dialog: DialogService,
  ) { }

  ngOnInit(): void {
    this.subscription = this.dialog.modalDialog$.subscribe(x => this.params = Object.assign({}, x));
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  onOk(event: any): void {

    event.stopPropagation();

    this.params.ok = true;
    this.params.show = false;
    this.dialog.modal(this.params);
  }

  onCancel(event: any): void {

    event.stopPropagation();

    this.params.ok = false;
    this.params.show = false;
    this.dialog.modal(this.params);
  }

  cancelEvent(event: any) {
    event.stopPropagation();
  }
}

export class ModalDialogParameter {

  show: boolean;
  title?: string;
  body: string[];
  okLabel: string;
  cancelLabel?: string;
  ok?: boolean;
}
