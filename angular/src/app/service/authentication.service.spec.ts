import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthenticationService } from './authentication.service';

describe('AuthenticationService', () => {

  let service: AuthenticationService;
  let httpSpy: jasmine.SpyObj<HttpClient>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {

    const spy1 = jasmine.createSpyObj<HttpClient>(['post', 'get']);
    const spy2 = jasmine.createSpyObj<Router>(['navigate']);
    TestBed.configureTestingModule({
      providers: [
        AuthenticationService,
        { provide: HttpClient, useValue: spy1 },
        { provide: Router, useValue: spy2 }
      ]
    });

    service = TestBed.get(AuthenticationService);
    httpSpy = TestBed.get(HttpClient);
    routerSpy = TestBed.get(Router);
  });

  it('ログイン要求POST', done => {

    // 想定値設定
    const username = 'user';
    const password = 'pass';
    const credentials = { username: username, password: password };
    const httpRet = of(true);
    const requestUrl = `${environment.restBaseUri}/auth/login`;

    // モック設定
    httpSpy.post.and.returnValue(httpRet);

    // テスト実行
    service.login(credentials)
      .then(actual => {
        // 遅延検証
        expect(actual).toBeTruthy();
        expect(service.authenticated).toBeTruthy();
        expect(httpSpy.post.calls.argsFor(0)).toEqual([requestUrl, credentials]);
        done();
      });
  });

  it('ログアウト要求POST', done => {

    // 想定値設定
    service.authenticated = true;
    const requestUrl = `${environment.restBaseUri}/auth/logout`;

    // モック設定
    httpSpy.post.and.returnValue(EMPTY);
    routerSpy.navigate.and.returnValue(of(true).toPromise());

    // テスト実行
    service.logout();
    setTimeout(() => {
      // 遅延検証
      expect(service.authenticated).toBeFalsy();
      expect(httpSpy.post.calls.argsFor(0)).toEqual([requestUrl, {}]);
      expect(routerSpy.navigate.calls.argsFor(0)).toEqual([['/']]);
      done();
    }, 0);
  });

  it('認証状況取得 認証済み', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/auth/user`;

    // モック設定
    httpSpy.get.and.returnValue(of({ name: 'y.maikuma' }));

    // テスト実行
    service.authenticate()
      .then(actual => {
        // 遅延検証
        expect(actual).toBeTruthy();
        expect(service.authenticated).toBeTruthy();
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
        done();
      });
  });

  it('認証状況取得 未認証', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/auth/user`;

    // モック設定
    httpSpy.get.and.returnValue(of({ name: 'anonymousUser' }));

    // テスト実行
    service.authenticate()
      .then(actual => {
        // 遅延検証
        expect(actual).toBeFalsy();
        expect(service.authenticated).toBeFalsy();
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
        done();
      });
  });
});
