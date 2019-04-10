import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { faSyncAlt } from '@fortawesome/free-solid-svg-icons';
import { ChartData } from 'src/app/component/common/line-chart/line-chart.component';
import { ExamHistory } from 'src/app/model/exam-history';
import { Examinee } from 'src/app/model/examinee';
import { Page } from 'src/app/model/page';
import { QuestionMode } from 'src/app/model/question-mode';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { ChartConstructionService } from 'src/app/service/chart-construction.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { HistoryService } from 'src/app/service/history.service';

@Component({
  selector: 'app-history-list',
  templateUrl: './history-list.component.html',
  styleUrls: ['./history-list.component.scss']
})
export class HistoryListComponent implements OnInit {

  examineeId = -999;
  examinees: Promise<Examinee[]>;
  historyPage: Page<ExamHistory>;

  chartVisible: boolean;
  chartData: ChartData;

  admin: boolean;

  questionMode: QuestionMode = QuestionMode.NORMAL;
  mode = QuestionMode;

  reloadIcon = faSyncAlt;

  constructor(
    private historyService: HistoryService,
    private router: Router,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
    private chartService: ChartConstructionService,
    private authService: AuthenticationService,
  ) { }

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number) {

    this.dialog.loading(true);
    this.chartVisible = false;
    this.authService.isAdmin().then(admin => {

      this.admin = admin;
      this.historyService.getHistoryPage(page, this.examineeId, this.questionMode).subscribe(x => {
        this.historyPage = x;
        if (x.content.length) {
          this.chartData = this.chartService.construct(
            x.content.slice().reverse().map(y => y.correctRate),
            x.content.slice().reverse().map(y => y.passingScore),
            x.content.slice().reverse().map(y => y.timestamp.toLocaleString()),
            '正答率');
        }
        if (admin) {
          this.examinees = this.historyService.getAllExaminees(this.questionMode);
          this.chartVisible = this.examineeId > 0 && this.questionMode === QuestionMode.NORMAL;
        } else if (x.content.length) {
          this.chartVisible = this.questionMode === QuestionMode.NORMAL;
        }
        this.dialog.loading(false);
        }, error => this.errorHandler.handle(error));
    });
  }

  onQuestionModeClick(): void {
    this.questionMode = this.questionMode === QuestionMode.NORMAL
      ? QuestionMode.FIXED
      : QuestionMode.NORMAL;
    this.loadPage(0);
  }

  onClickReloadIcon(): void {
    this.loadPage(this.historyPage.currentNumber - 1);
  }

  toDetail(examHistory: ExamHistory): void {

    let queryParams = {};
    if (this.questionMode === QuestionMode.FIXED) {
      queryParams = {
        examineeId: examHistory.examineeId,
        examNo: examHistory.examNo,
        fixedQuestionsId: examHistory.fixedQuestionsId,
        examCount: examHistory.examCount,
      };
    } else {
      queryParams = {
        examineeId: examHistory.examineeId,
        examNo: examHistory.examNo,
        examCoverage: examHistory.examCoverage,
        examCount: examHistory.examCount
      };
    }

    this.router.navigate(['/history/detail'], { queryParams: queryParams });
  }
}
