import { TestBed } from '@angular/core/testing';
import { environment } from 'src/environments/environment';
import { InitService } from './init.service';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';

describe('InitService', () => {

  let service: InitService;
  let http: jasmine.SpyObj<HttpClient>;

  beforeEach(() => {

    const spy = jasmine.createSpyObj<HttpClient>(['get']);
    TestBed.configureTestingModule({
      providers: [
        InitService,
        { provide: HttpClient, useValue: spy },
      ],
    });

    service = TestBed.get(InitService);
    http = TestBed.get(HttpClient);
  });

  it('全ての受験者を取得', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/examinee`;

    // モック設定
    http.get.and.returnValue(of([]));

    // 試験実行
    service.getExaminees().then(actual => {

      // 遅延検証
      expect(actual).toEqual([]);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('全ての試験種別を取得', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/exam`;

    // モック設定
    http.get.and.returnValue(of([]));

    // 試験実行
    service.getExams().then(actual => {

      // 遅延検証
      expect(actual).toEqual([]);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('試験種別に紐付く試験範囲を取得', done => {

    // 想定値設定
    const examNo = 1;
    const requestUrl = `${environment.restBaseUri}/coverage/${examNo}`;

    // モック設定
    http.get.and.returnValue(of([]));

    // 試験実行
    service.getCoverages(examNo).then(actual => {

      // 遅延検証
      expect(actual).toEqual([]);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('削除済みを含む試験範囲を取得', done => {

    // 想定値設定
    const examNo = 1;
    const requestUrl = `${environment.restBaseUri}/coverage/includeDeleted/${examNo}`;

    // モック設定
    http.get.and.returnValue(of([]));

    // 試験実行
    service.getCoveragesIncludeDeleted(examNo).then(actual => {

      // 遅延検証
      expect(actual).toEqual([]);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('試験種別に紐付く固定出題範囲を取得', done => {

    // 想定値設定
    const examNo = 1;
    const requestUrl = `${environment.restBaseUri}/fixed/${examNo}`;

    // モック設定
    http.get.and.returnValue(of([]));

    // 試験実行
    service.getFixedQuestions(examNo).then(actual => {

      // 遅延検証
      expect(actual).toEqual([]);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });
});
