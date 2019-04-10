import { environment } from 'src/environments/environment';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CookieService } from 'ngx-cookie-service';
import { QuestionMode } from '../model/question-mode';
import { HistoryService } from './history.service';
import { XhrInterceptorService } from './xhr-interceptor.service';

describe('XhrInterceptorService', () => {

  let historyService: HistoryService;
  let httpMock: HttpTestingController;
  let cookieService: jasmine.SpyObj<CookieService>;

  beforeEach(() => {

    const cookieSpy = jasmine.createSpyObj<CookieService>(['get']);

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        HistoryService,
        { provide: HTTP_INTERCEPTORS, useClass: XhrInterceptorService, multi: true },
        { provide: CookieService, useValue: cookieSpy },
      ]
    });

    historyService = TestBed.get(HistoryService);
    httpMock = TestBed.get(HttpTestingController);
    cookieService = TestBed.get(CookieService);
  });

  it('XSRFトークンとセッションIDをリクエストヘッダに付け加える', () => {

    // 想定値設定
    const xsrfToken = 'xsrfToken';
    const sessionId = 'sessionId';

    // モック設定
    cookieService.get.and.returnValues(xsrfToken, sessionId);

    // 試験実行
    historyService.getHistoryPage(1, -999, QuestionMode.NORMAL)
      .subscribe(x => expect(x).toBeTruthy());
    const httpRequest = httpMock.expectOne(`${environment.restBaseUri}/history?page=1&questionMode=NORMAL`);
    expect(httpRequest.request.headers.get('X-Requested-With')).toEqual('XMLHttpRequest');
    expect(httpRequest.request.headers.get('X-XSRF-TOKEN')).toEqual(xsrfToken);
    expect(httpRequest.request.headers.get('X-AUTH-TOKEN')).toEqual(sessionId);
  });
});
