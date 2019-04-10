import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { EMPTY, of } from 'rxjs';
import { Exam } from '../model/exam';
import { FixedQuestion } from './../model/fixed-question';
import { EditQuestionService } from './edit-question.service';
import { environment } from 'src/environments/environment';

describe('EditQuestionService', () => {

  let service: EditQuestionService;
  let http: jasmine.SpyObj<HttpClient>;

  beforeEach(() => {

    const httpSpy = jasmine.createSpyObj<HttpClient>(['get', 'post', 'put', 'delete']);
    TestBed.configureTestingModule({
      providers: [
        EditQuestionService,
        { provide: HttpClient, useValue: httpSpy }
      ]
    });

    service = TestBed.get(EditQuestionService);
    http = TestBed.get(HttpClient);
  });

  it('指定の固定出題データを取得', done => {

    // 想定値設定
    const fixedQuestion: FixedQuestion = {
      id: 1,
      examNo: 2,
      name: '固定出題名',
      questions: '1,2,3',
      modifiedAt: new Date('2019-01-01T12:34:56'),
    };
    const fixedQuestionsId = 1;
    const examNo = 2;
    const requestUrl = `${environment.restBaseUri}/fixed/2/1`;
    const httpRet = of(fixedQuestion);

    // モック設定
    http.get.and.returnValue(httpRet);

    // 試験実行
    service.getFixedQuestion(fixedQuestionsId, examNo).subscribe(actual => {
      // 遅延検証
      expect(actual).toEqual(fixedQuestion);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('固定出題バリデーション', done => {

    // 想定値設定
    const fixedQuestion: FixedQuestion = {
      id: 1,
      examNo: 2,
      name: '固定出題名',
      questions: '1,2,3',
      modifiedAt: new Date('2019-01-01T12:34:56'),
    };
    const requestUrl = `${environment.restBaseUri}/fixed/validate`;
    const httpRet = of({ 'fixedQuestionsId': ['エラーメッセージ'] });

    // モック設定
    http.post.and.returnValue(httpRet);

    // 試験実行
    service.validateFixedQuestion(fixedQuestion).then(x => {
      // 遅延検証
      expect(x).toEqual({ 'fixedQuestionsId': ['エラーメッセージ'] });
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, fixedQuestion]);
      done();
    });
  });

  it('固定出題データ登録', done => {

    // 想定値設定
    const fixedQuestion: FixedQuestion = {
      id: undefined,
      examNo: 2,
      name: '固定出題名',
      questions: '1,2,3',
      modifiedAt: undefined,
    };
    const requestUrl = `${environment.restBaseUri}/fixed`;

    // モック設定
    http.put.and.returnValue(EMPTY);

    // 試験実行
    service.insertFixedQuestion(fixedQuestion).then(() => {
      // 遅延検証
      expect(http.put.calls.argsFor(0)).toEqual([requestUrl, fixedQuestion]);
      done();
    });
  });

  it('固定出題データ更新', done => {

    // 想定値設定
    const fixedQuestion: FixedQuestion = {
      id: 1,
      examNo: 2,
      name: '固定出題名',
      questions: '1,2,3',
      modifiedAt: new Date('2019-01-01T12:34:56'),
    };
    const requestUrl = `${environment.restBaseUri}/fixed`;

    // モック設定
    http.post.and.returnValue(EMPTY);

    // 試験実行
    service.updateFixedQuestion(fixedQuestion).then(() => {
      // 遅延検証
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, fixedQuestion]);
      done();
    });
  });

  it('固定出題データ削除', done => {

    // 想定値設定
    const fixedQuestionsId = 1;
    const examNo = 2;
    const requestUrl = `${environment.restBaseUri}/fixed/2/1`;

    // モック設定
    http.delete.and.returnValue(EMPTY);

    // 試験実行
    service.deleteFixedQuestion(examNo, fixedQuestionsId).then(() => {
      // 遅延検証
      expect(http.delete.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('指定の試験種別データを取得', done => {

    // 想定値設定
    const exam: Exam = {
      examNo: 1,
      examName: '試験名',
      passingScore: 65,
      deleted: false,
      modifiedAt: new Date('2018-01-01T12:34:56')
    };
    const examNo = 1;
    const requestUrl = `${environment.restBaseUri}/exam/1`;
    const httpRet = of(exam);

    // モック設定
    http.get.and.returnValue(httpRet);

    // 試験実行
    service.getExam(examNo).subscribe(x => {
      // 遅延検証
      expect(x).toEqual(exam);
      expect(http.get.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });

  it('試験種別バリデーション', done => {

    // 想定値設定
    const exam: Exam = {
      examNo: 1,
      examName: '試験名',
      passingScore: 65,
      deleted: false,
      modifiedAt: new Date('2018-01-01T12:34:56')
    };
    const requestUrl = `${environment.restBaseUri}/exam/validate`;
    const ret: { [key: string]: string[] } = { 'examNo': ['エラーメッセージ'] };
    const httpRet = of(ret);

    // モック設定
    http.post.and.returnValue(httpRet);

    // 試験実行
    service.validateExam(exam).then(x => {
      // 遅延検証
      expect(x).toEqual(ret);
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, exam]);
      done();
    });
  });

  it('試験種別データ登録', done => {

    // 想定値設定
    const exam: Exam = {
      examNo: undefined,
      examName: '試験名',
      passingScore: 65,
      deleted: undefined,
      modifiedAt: undefined
    };
    const requestUrl = `${environment.restBaseUri}/exam`;

    // モック設定
    http.put.and.returnValue(EMPTY);

    // 試験実行
    service.insertExam(exam).then(() => {
      // 遅延検証
      expect(http.put.calls.argsFor(0)).toEqual([requestUrl, exam]);
      done();
    });
  });

  it('試験種別データ更新', done => {

    // 想定値設定
    const exam: Exam = {
      examNo: 1,
      examName: '試験名',
      passingScore: 65,
      deleted: false,
      modifiedAt: new Date('2018-01-01T12:34:56')
    };
    const requestUrl = `${environment.restBaseUri}/exam`;

    // モック設定
    http.post.and.returnValue(EMPTY);

    // 試験実行
    service.updateExam(exam).then(() => {
      // 遅延検証
      expect(http.post.calls.argsFor(0)).toEqual([requestUrl, exam]);
      done();
    });
  });

  it('試験種別データ削除', done => {

    // 想定値設定
    const examNo = 1;
    const requestUrl = `${environment.restBaseUri}/exam/1`;

    // モック設定
    http.delete.and.returnValue(EMPTY);

    // 試験実行
    service.deleteExam(examNo).then(() => {
      // 遅延検証
      expect(http.delete.calls.argsFor(0)).toEqual([requestUrl]);
      done();
    });
  });
});
