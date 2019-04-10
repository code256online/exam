import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { of, throwError } from 'rxjs';
import { CommonComponentModule } from 'src/app/component/common/common-component.module';
import { ChartData } from 'src/app/component/common/line-chart/line-chart.component';
import { ExamHistory } from 'src/app/model/exam-history';
import { Examinee } from 'src/app/model/examinee';
import { Page } from 'src/app/model/page';
import { QuestionMode } from 'src/app/model/question-mode';
import { ChartConstructionService } from 'src/app/service/chart-construction.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { HistoryService } from 'src/app/service/history.service';
import { InitService } from 'src/app/service/init.service';
import { HistoryListComponent } from './history-list.component';

describe('HistoryListComponent', () => {

  const examinees1: Examinee[] = require('src/assets/test-data/component/history-list/examinees1.json');
  const historyPage1: Page<ExamHistory> = require('src/assets/test-data/component/history-list/history-page1.json');
  const historyPage2: Page<ExamHistory> = require('src/assets/test-data/component/history-list/history-page2.json');
  const fixedHistoryPage1: Page<ExamHistory> = require('src/assets/test-data/component/history-list/history-page-fixed1.json');
  const chartData1: ChartData = require('src/assets/test-data/component/history-list/chart-data1.json');

  let initService: jasmine.SpyObj<InitService>;
  let historyService: jasmine.SpyObj<HistoryService>;
  let router: jasmine.SpyObj<Router>;
  let errorHandler: jasmine.SpyObj<DefaultErrorHandlerService>;
  let dialog: jasmine.SpyObj<DialogService>;
  let chartService: jasmine.SpyObj<ChartConstructionService>;

  let fixture: ComponentFixture<HistoryListComponent>;
  let component: HistoryListComponent;
  let debugElement: DebugElement;

  beforeEach(async(() => {

    const initServiceSpy = jasmine.createSpyObj<InitService>(['getExaminees']);
    const historyServiceSpy = jasmine.createSpyObj<HistoryService>(['getHistoryPage']);
    const routerSpy = jasmine.createSpyObj<Router>(['navigate']);
    const errorHandlerSpy = jasmine.createSpyObj<DefaultErrorHandlerService>(['handle']);
    const dialogSpy = jasmine.createSpyObj<DialogService>(['loading']);
    const chartServiceSpy = jasmine.createSpyObj<ChartConstructionService>(['construct']);
    TestBed.configureTestingModule({
      declarations: [
        HistoryListComponent,
      ],
      providers: [
        { provide: InitService, useValue: initServiceSpy },
        { provide: HistoryService, useValue: historyServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: DefaultErrorHandlerService, useValue: errorHandlerSpy },
        { provide: DialogService, useValue: dialogSpy },
        { provide: ChartConstructionService, useValue: chartServiceSpy },
      ],
      imports: [
        FormsModule,
        CommonComponentModule,
        FontAwesomeModule,
      ]
    }).compileComponents();

    initService = TestBed.get(InitService);
    historyService = TestBed.get(HistoryService);
    router = TestBed.get(Router);
    errorHandler = TestBed.get(DefaultErrorHandlerService);
    dialog = TestBed.get(DialogService);
    chartService = TestBed.get(ChartConstructionService);

    // モック設定
    initService.getExaminees.and.returnValue(of(examinees1).toPromise());
    dialog.loading.and.callThrough();
    historyService.getHistoryPage.and.returnValue(of(historyPage1));

    // 試験実行
    fixture = TestBed.createComponent(HistoryListComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  }));

  it('初期表示', async(() => {

    // サービス呼び出し検証
    expect(initService.getExaminees.calls.count()).toEqual(1);
    expect(dialog.loading.calls.argsFor(0)).toEqual([true]);
    expect(historyService.getHistoryPage.calls.argsFor(0)).toEqual([0, -999, QuestionMode.NORMAL]);
    expect(dialog.loading.calls.argsFor(1)).toEqual([false]);

    fixture.whenStable().then(() => {

      // Promise が帰ってきてからDOM検証
      fixture.detectChanges();

      // 絞込みしない場合チャートは表示しない
      expect(component.chartVisible).toBeFalsy();

      // 「全てのユーザー」を含めて3つの<option>がある
      const options: DebugElement[] = debugElement.queryAll(By.css('#examinees option'));
      expect(options.length).toEqual(3);
      expect(options[0].nativeElement.innerText).toEqual('全てのユーザー');
      expect(options[0].nativeElement.value).toEqual('-999');
      expect(options[1].nativeElement.innerText).toEqual('名前１');
      expect(options[1].nativeElement.value).toEqual('1');
      expect(options[2].nativeElement.innerText).toEqual('名前２');
      expect(options[2].nativeElement.value).toEqual('2');

      // 2行帰ってきたので2行表示する
      const tableContents: DebugElement[] = debugElement.queryAll(By.css('tbody tr'));
      expect(tableContents.length).toEqual(2);
      // 1行目の中身確認
      const row = tableContents[0];
      expect(row.queryAll(By.css('th'))[0].nativeElement.innerText).toEqual('名前１');
      expect(row.queryAll(By.css('th'))[1].nativeElement.innerText).toEqual('試験１');
      expect(row.queryAll(By.css('th'))[2].nativeElement.innerText).toEqual('全ての範囲');
      expect(row.queryAll(By.css('td'))[0].nativeElement.innerText).toEqual('10問');
      expect(row.queryAll(By.css('td'))[1].nativeElement.innerText).toEqual('9問 (90％)');
      expect(row.queryAll(By.css('td'))[2].nativeElement.innerText).toEqual('8問 (80％)');
      expect(row.queryAll(By.css('td'))[3].nativeElement.innerText).toEqual('2018/11/11 12:34:56');
    });
  }));

  it('受験者で絞込み', async(() => {

    fixture.whenStable().then(() => {

      fixture.detectChanges();
      // モック設定
      dialog.loading.and.callThrough();
      historyService.getHistoryPage.and.returnValue(of(historyPage2));
      chartService.construct.and.returnValue(chartData1);

      const selectElement = debugElement.query(By.css('#examinees')).nativeElement;
      selectElement.value = '1';
      selectElement.dispatchEvent(new Event('change'));
      fixture.detectChanges();

      // サービス呼び出し検証
      expect(dialog.loading.calls.argsFor(2)).toEqual([true]);
      expect(historyService.getHistoryPage.calls.argsFor(1)).toEqual([0, '1', QuestionMode.NORMAL]);
      expect(chartService.construct.calls.argsFor(1)).toEqual([
        historyPage2.content.slice().reverse().map(x => x.correctRate),
        historyPage2.content.slice().reverse().map(x => x.passingScore),
        historyPage2.content.slice().reverse().map(x => x.timestamp.toLocaleString())
      ]);
      expect(dialog.loading.calls.argsFor(3)).toEqual([false]);

      // 絞込みする場合はチャートを表示
      expect(component.chartVisible).toBeTruthy();

      // 2行帰ってきたので2行表示する
      const tableContents: DebugElement[] = debugElement.queryAll(By.css('tbody tr'));
      expect(tableContents.length).toEqual(2);
      // 2行目の中身確認
      const row = tableContents[1];
      expect(row.queryAll(By.css('th'))[0].nativeElement.innerText).toEqual('名前１');
      expect(row.queryAll(By.css('th'))[1].nativeElement.innerText).toEqual('試験１');
      expect(row.queryAll(By.css('th'))[2].nativeElement.innerText).toEqual('範囲２');
      expect(row.queryAll(By.css('td'))[0].nativeElement.innerText).toEqual('5問');
      expect(row.queryAll(By.css('td'))[1].nativeElement.innerText).toEqual('4問 (80％)');
      expect(row.queryAll(By.css('td'))[2].nativeElement.innerText).toEqual('3問 (60％)');
      expect(row.queryAll(By.css('td'))[3].nativeElement.innerText).toEqual('2018/11/09 12:34:56');
    });
  }));

  it('リロードアイコンを押したら現在ページ番号を再取得', async(() => {

    fixture.whenStable().then(() => {

      component.historyPage.currentNumber = 22;
      debugElement.query(By.css('#reloadIcon')).nativeElement.click();
      fixture.detectChanges();

      expect(dialog.loading.calls.argsFor(2)).toEqual([true]);
      expect(historyService.getHistoryPage.calls.argsFor(1)).toEqual([21, -999, QuestionMode.NORMAL]);
      expect(dialog.loading.calls.argsFor(3)).toEqual([false]);
      // DOM の検証省略
    });
  }));

  it('固定出題モードチェック切り替え時は0ページ目を表示', async(() => {

    fixture.whenStable().then(() => {

      // モック設定
      historyService.getHistoryPage.and.returnValue(of(fixedHistoryPage1));

      debugElement.query(By.css('#questionType')).nativeElement.click();
      fixture.detectChanges();

      expect(dialog.loading.calls.argsFor(2)).toEqual([true]);
      expect(historyService.getHistoryPage.calls.argsFor(1)).toEqual([0, -999, QuestionMode.FIXED]);
      expect(dialog.loading.calls.argsFor(3)).toEqual([false]);
      // DOM の検証省略
    });
  }));

  it('ページ取得リクエストでエラーのときにエラーダイアログ', async(() => {

    fixture.whenStable().then(() => {

      // モック設定
      const err = new Error('エラー');
      historyService.getHistoryPage.and.returnValue(throwError(err));

      component.questionMode = QuestionMode.FIXED;
      debugElement.query(By.css('#questionType')).nativeElement.click();
      fixture.detectChanges();

      expect(dialog.loading.calls.argsFor(2)).toEqual([true]);
      expect(historyService.getHistoryPage.calls.argsFor(1)).toEqual([0, -999, QuestionMode.NORMAL]);
      expect(errorHandler.handle.calls.argsFor(0)).toEqual([err]);
      // DOM の検証省略
    });
  }));

  it('通常モードで明細クリックすると通常モードの詳細ページへ', async(() => {

    fixture.whenStable().then(() => {

      // クエリパラメータ想定値
      const queryParams = require('src/assets/test-data/component/history-list/detail-query-normal1.json');

      // モック設定
      router.navigate.and.returnValue(true);

      // 明細 1 行目クリック
      debugElement.queryAll(By.css('tbody tr'))[0].nativeElement.click();
      fixture.detectChanges();

      expect(router.navigate.calls.argsFor(0)).toEqual([['/history/detail'], queryParams]);
    });
  }));

  it('固定出題モードで明細クリックすると固定出題モードの詳細ページへ', async(() => {

    fixture.whenStable().then(() => {

      // モック設定
      historyService.getHistoryPage.and.returnValue(of(fixedHistoryPage1));

      debugElement.query(By.css('#questionType')).nativeElement.click();
      fixture.detectChanges();

      // クエリパラメータ想定値
      const queryParams = require('src/assets/test-data/component/history-list/detail-query-fixed1.json');

      // モック設定
      router.navigate.and.returnValue(true);

      // 明細 1 行目クリック
      debugElement.queryAll(By.css('tbody tr'))[0].nativeElement.click();
      fixture.detectChanges();

      expect(router.navigate.calls.argsFor(0)).toEqual([['/history/detail'], queryParams]);
    });
  }));
});
