import { TestBed } from '@angular/core/testing';
import { DefaultErrorHandlerService } from './default-error-handler.service';
import { DialogService } from './dialog.service';
import { errorModalParams } from '../../environments/constants';

describe('DefaultErrorHandlerService', () => {

  let service: DefaultErrorHandlerService;
  let dialog: jasmine.SpyObj<DialogService>;

  beforeEach(() => {

    const dialogSpy = jasmine.createSpyObj<DialogService>(['loading', 'error']);
    TestBed.configureTestingModule({
      providers: [
        DefaultErrorHandlerService,
        { provide: DialogService, useValue: dialogSpy },
      ]
    });

    service = TestBed.get(DefaultErrorHandlerService);
    dialog = TestBed.get(DialogService);
  });

  it('セッションタイムアウト以外', () => {

    // 想定値設定
    const error = { error: { status: 500, message: 'Internal Server Error.' } };
    const dialogParams = errorModalParams.default;
    dialogParams.trace = JSON.stringify(error, null, 2);

    // モック設定
    dialog.loading.and.stub();
    dialog.error.and.stub();

    // テスト実行
    service.handle(error);

    // 検証
    expect(dialog.loading.calls.argsFor(0)).toEqual([false]);
    expect(dialog.error.calls.argsFor(0)).toEqual([dialogParams]);
  });

  it('セッションタイムアウト', () => {

    // 想定値設定
    const error = { error: { status: 400, message: 'Invalid Session Detected.' } };
    const dialogParams = errorModalParams.timeout;

    // モック設定
    dialog.loading.and.stub();
    dialog.error.and.stub();

    // テスト実行
    service.handle(error);

    // 検証
    expect(dialog.loading.calls.argsFor(0)).toEqual([false]);
    expect(dialog.error.calls.argsFor(0)).toEqual([dialogParams]);
  });
});
