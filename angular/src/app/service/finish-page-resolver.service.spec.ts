import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ExamHistory } from '../model/exam-history';
import { Page } from '../model/page';
import { FinishPageResolverService } from './finish-page-resolver.service';
import { QuestionService } from './question.service';


describe('FinishPageResolverService', () => {

  let service: FinishPageResolverService;
  let questionService: jasmine.SpyObj<QuestionService>;

  beforeEach(() => {
    const questionServiceSpy = jasmine.createSpyObj<QuestionService>(['getFinishPage']);
    TestBed.configureTestingModule({
      providers: [
        FinishPageResolverService,
        { provide: QuestionService, useValue: questionServiceSpy },
      ],
    });

    service = TestBed.get(FinishPageResolverService);
    questionService = TestBed.get(QuestionService);
  });

  it('解答終了ページ表示情報を事前取得', done => {

    // 想定値設定
    const page: Page<ExamHistory> = new Page<ExamHistory>();

    // モック設定
    questionService.getFinishPage.and.returnValue(of(page));

    // 試験実行
    service.resolve(null, null).subscribe(x => {
      // 遅延検証
      expect(x).toEqual(page);
      expect(questionService.getFinishPage.calls.count()).toEqual(1);
      done();
    });
  });
});
