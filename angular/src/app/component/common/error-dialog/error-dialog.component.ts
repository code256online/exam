import { Component, OnDestroy, OnInit, InjectionToken, Inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { DialogService } from 'src/app/service/dialog.service';

export const LOCATION_TOKEN = new InjectionToken<Location>('window location');

@Component({
  selector: 'app-error-dialog',
  templateUrl: './error-dialog.component.html',
  styleUrls: ['./error-dialog.component.scss'],
  providers: [
    { provide: LOCATION_TOKEN, useValue: window.location }
  ]
})
export class ErrorDialogComponent implements OnInit, OnDestroy {

  subscription: Subscription;
  params: ErrorDialogParameter;

  constructor(
    private dialog: DialogService,
    @Inject(LOCATION_TOKEN) private location: Location,
  ) { }

  ngOnInit() {
    this.subscription = this.dialog.errorDialog$.subscribe(x => this.params = Object.assign({}, x));
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  onOk(event: any): void {

    event.stopPropagation();
    this.params.show = false;
    this.location.assign('/exam');
  }

  onCopy(): void {
    document.getSelection().selectAllChildren(document.getElementById('trace'));
    document.execCommand('copy');
    document.getSelection().empty();
  }

  cancelEvent(event: any) {
    event.stopPropagation();
  }
}

export class ErrorDialogParameter {

  show: boolean;
  title?: string;
  body: string[];
  trace?: string;
}
