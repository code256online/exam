import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { InitData } from '../model/init-data';

@Injectable({
  providedIn: 'root'
})
export class InitDataBindService {

  private subject = new Subject<InitData>();

  private _initData: InitData = new InitData();
  get initData(): InitData {
    return this._initData;
  }
  set initData(initData: InitData) {
    this._initData = initData;
    this.subject.next(initData);
  }

  initData$ = this.subject.asObservable();

  constructor() { }

  resetStates(): void {
    this.subject.next(new InitData());
  }
}
