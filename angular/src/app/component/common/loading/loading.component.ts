import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { DialogService } from 'src/app/service/dialog.service';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss']
})
export class LoadingComponent implements OnInit, OnDestroy {

  subscription: Subscription;
  loading: boolean;

  constructor(
    private dialog: DialogService,
  ) { }

  ngOnInit(): void {
    this.subscription = this.dialog.loading$.subscribe(x => {
      this.loading = x;
      window.scrollTo(0, 0);
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
