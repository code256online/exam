import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ExamHistory } from '../model/exam-history';
import { Page } from '../model/page';
import { QuestionMode } from './../model/question-mode';
import { HistoryService } from './history.service';

describe('HistoryService', () => {

  let service: HistoryService;
  let httpSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(() => {

    const spy = jasmine.createSpyObj<HttpClient>(['get']);

    TestBed.configureTestingModule({
      providers: [
        HistoryService,
        { provide: HttpClient, useValue: spy }
      ]
    });

    service = TestBed.get(HistoryService);
    httpSpy = TestBed.get(HttpClient);
  });

  it('通常モード、受験者指定なしの履歴取得', () => {

    // 想定値設定
    const page = 1;
    const examineeId = -999;
    const questionMode = QuestionMode.NORMAL;
    const httpRet = of(new Page<ExamHistory>());
    const requestUrl = `${environment.restBaseUri}/history?page=1&questionMode=NORMAL`;

    // モック設定
    httpSpy.get.and.returnValue(httpRet);

    // 試験実行
    service.getHistoryPage(page, examineeId, questionMode)
      .subscribe(actual => {
        // 検証
        expect(actual).toEqual(new Page<ExamHistory>());
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
      });
  });

  it('固定出題モード、受験者指定ありの履歴取得', () => {

    // 想定値設定
    const page = 0;
    const examineeId = 3;
    const questionMode = QuestionMode.FIXED;
    const httpRet = of(new Page<ExamHistory>());
    const requestUrl = `${environment.restBaseUri}/history?page=0&examineeId=3&questionMode=FIXED`;

    // モック設定
    httpSpy.get.and.returnValue(httpRet);

    // 試験実行
    service.getHistoryPage(page, examineeId, questionMode)
      .subscribe(actual => {
        // 検証
        expect(actual).toEqual(new Page<ExamHistory>());
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
      });
  });

  it('通常モード履歴詳細ページの取得', () => {

    // 想定値設定
    const examineeId = 2;
    const examNo = 1;
    const examCoverage = -1;
    const examCount = 4;
    const httpRetVal = new Page<ExamHistory>();
    httpRetVal.content = [new ExamHistory()];
    const httpRet = of(httpRetVal);
    const requestUrl = `${environment.restBaseUri}/history/detail?examineeId=2&examNo=1&examCoverage=-1&examCount=4`;

    // モック設定
    httpSpy.get.and.returnValue(httpRet);

    // 試験実行
    service.getDetailPage(examineeId, examNo, examCoverage, examCount)
      .subscribe(actual => {
        // 検証
        expect(actual).toEqual(new ExamHistory());
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
      });
  });

  it('固定出題モード詳細ページ取得', () => {

    // 想定値設定
    const examineeId = 2;
    const examNo = 1;
    const fixedQuestionsId = 3;
    const examCount = 4;
    const httpRetVal = new Page<ExamHistory>();
    httpRetVal.content = [new ExamHistory()];
    const httpRet = of(httpRetVal);
    const requestUrl = `${environment.restBaseUri}/history/fixedDetail?examineeId=2&examNo=1&fixedQuestionsId=3&examCount=4`;

    // モック設定
    httpSpy.get.and.returnValue(httpRet);

    // 試験実行
    service.getFixedDetailPage(examineeId, examNo, fixedQuestionsId, examCount)
      .subscribe(actual => {
        // 検証
        expect(actual).toEqual(new ExamHistory());
        expect(httpSpy.get.calls.argsFor(0)).toEqual([requestUrl]);
      });
  });
});
