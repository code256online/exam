import { ExamHistory } from './../model/exam-history';
import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, EMPTY } from 'rxjs';
import { environment } from 'src/environments/environment';
import { QuestionService } from './question.service';
import { Question } from '../model/question';
import { Page } from '../model/page';

describe('QuestionService', () => {

  let service: QuestionService;
  let http: jasmine.SpyObj<HttpClient>;

  beforeEach(() => {

    const spy = jasmine.createSpyObj<HttpClient>(['get', 'post']);

    TestBed.configureTestingModule({
      providers: [
        QuestionService,
        { provide: HttpClient, useValue: spy }
      ]
    });

    service = TestBed.get(QuestionService);
    http = TestBed.get(HttpClient);
  });

  it('セッションに未終了の試験があるか調べる', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/question/resumeCheck`;

    // モック設定
    http.get.and.returnValue(of(true));

    // 試験実行
    service.resumeCheck().then(actual => {

      // 検証
      expect(actual).toBeTruthy();
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('セッションの出題履歴のリセット要求', () => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/question/reset`;

    // モック設定
    http.get.and.returnValue(EMPTY);

    // 試験実行
    service.reset();

    // 検証
    expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
  });

  it('初期設定フォームのバリデーション要求', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/question/initValidation`;
    const form = require('src/assets/test-data/service/question-service/init-data1.json');
    const expected = {};

    // モック設定
    http.post.and.returnValue(of(expected));

    // 試験実行
    service.initValidation(form).then(actual => {

      // 検証
      expect(actual).toEqual(expected);
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, form]);
      done();
    });
  });

  it('初期出題ページ取得', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/question/init`;
    const form = require('src/assets/test-data/service/question-service/init-data1.json');
    const expected = new Page<Question>();

    // モック設定
    http.post.and.returnValue(of(expected));

    // 試験実行
    service.initialize(form).subscribe(actual => {

      // 検証
      expect(actual).toEqual(expected);
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, form]);
      done();
    });
  });

  it('単一選択の答えあわせと次ページ取得の要求', done => {

    // 想定値設定
    const page = 1;
    const r = 'A';
    const requestUrl = `${environment.restBaseUri}/question?page=${page}&r=${r}`;
    const expected = new Page<Question>();

    // モック設定
    http.get.and.returnValue(of(expected));

    // 試験実行
    service.getQuestionPage(page, {}, r).subscribe(actual => {

      // 検証
      expect(actual).toEqual(expected);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('複数選択の最終問題解答送信', done => {

    // 想定値設定
    const page = 1;
    const c = {
      'A': true,
      'B': false,
      'C': false,
      'D': true,
      'E': false,
    };
    const requestUrl = `${environment.restBaseUri}/question/finish?c=A&c=D`;

    // モック設定
    http.get.and.returnValue(EMPTY);

    // 試験実行
    service.finish(c, undefined).then(() => {

      // 検証
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('結果ページの取得要求', done => {

    // 想定値設定
    const requestUrl = `${environment.restBaseUri}/question/finishPage`;
    const expected = new Page<ExamHistory>();

    // モック設定
    http.get.and.returnValue(of(expected));

    // 試験実行
    service.getFinishPage().subscribe(actual => {

      // 検証
      expect(actual).toEqual(expected);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });
});
