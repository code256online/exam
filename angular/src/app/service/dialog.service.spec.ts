import { TestBed } from '@angular/core/testing';
import { first, skip } from 'rxjs/operators';
import { ErrorDialogParameter } from '../component/common/error-dialog/error-dialog.component';
import { LoginDialogParameter } from './../component/common/login/login.component';
import { ModalDialogParameter } from './../component/common/modal-dialog/modal-dialog.component';
import { DialogService } from './dialog.service';

describe('DialogService', () => {

  let service: DialogService;

  beforeEach(() => {

    TestBed.configureTestingModule({
      providers: [
        DialogService,
      ]
    });

    service = TestBed.get(DialogService);
  });

  it('エラーダイアログのパラメータを流す', done => {

    // 想定値設定
    const params: ErrorDialogParameter = {
      show: true,
      title: 'タイトル',
      body: [
        '本文'
      ],
      trace: 'スタックトレース',
    };

    // 検証
    service.errorDialog$.subscribe(x => {
      expect(x).toEqual(params);
      done();
    });

    // 試験実行
    service.error(params);
  });

  it('モーダルダイアログのパラメータを流す。閉じたときPromiseを流す。', done => {

    // 想定値設定
    const params1: ModalDialogParameter = {
      show: true,
      title: 'タイトル',
      body: [
        '本文'
      ],
      okLabel: 'OK',
      cancelLabel: 'Cancel',
    };

    const params2: ModalDialogParameter = {
      show: false,
      title: 'タイトル',
      body: [
        '本文'
      ],
      okLabel: 'OK',
      cancelLabel: 'Cancel',
      ok: true
    };

    // 検証と試験実行
    service.modalDialog$.pipe(first()).subscribe(x => {
      expect(x).toEqual(params1);
    });
    service.modalDialog$.pipe(skip(1)).subscribe(x => {
      expect(x).toEqual(params2);
    });

    service.modal(params1).subscribe(x => {
      expect(x).toBeTruthy();
      done();
    });
    service.modal(params2);
  });

  it('ローディングスクリーンのパラメータを流す', done => {

    // 想定値設定
    const params = true;

    // 検証
    service.loading$.subscribe(x => {
      expect(x).toBeTruthy();
      done();
    });

    // 試験実行
    service.loading(true);
  });

  it('ログインダイアログのパラメータを流す', done => {

    // 想定値設定
    const params: LoginDialogParameter = {
      show: true,
      redirectTo: '/hoge'
    };

    // 検証
    service.login$.subscribe(x => {
      expect(x).toEqual(params);
      done();
    });

    // 試験実行
    service.login(params);
  });
});
