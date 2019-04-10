import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { faSyncAlt } from '@fortawesome/free-solid-svg-icons';
import { take } from 'rxjs/operators';
import { InitData } from 'src/app/model/init-data';
import { Page } from 'src/app/model/page';
import { Question } from 'src/app/model/question';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { InitDataBindService } from 'src/app/service/init-data-bind.service';
import { QuestionService } from 'src/app/service/question.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-question-set',
  templateUrl: './question-set.component.html',
  styleUrls: ['./question-set.component.scss']
})
export class QuestionSetComponent implements OnInit, OnDestroy {

  page: Page<Question>;
  initData: InitData;

  m: string;
  s: string;
  timer: NodeJS.Timeout;

  c: { [key: string]: boolean } = {};
  r: string;

  reloadIcon = faSyncAlt;

  constructor(
    private questionService: QuestionService,
    private dataBinder: InitDataBindService,
    private router: Router,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
  ) { }

  ngOnInit(): void {
    this.dialog.loading(true);
    this.questionService.initialize(this.dataBinder.initData)
      .subscribe(x => this.setupPage(x),
        error => this.errorHandler.handle(error));
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  nextPage(): void {
    this.dialog.loading(true);
    this.questionService.getQuestionPage(this.page.currentNumber, this.c, this.r)
      .subscribe(x => this.setupPage(x),
        error => this.errorHandler.handle(error));
  }

  getPage(page: number): void {
    this.dialog.loading(true);
    this.questionService.getQuestionPage(page, this.c, this.r)
      .subscribe(x => this.setupPage(x),
        error => this.errorHandler.handle(error));
  }

  finish(): void {
    this.dialog.modal(modalParams.confirmFinish).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.questionService.finish(this.c, this.r)
            .then(() => this.router.navigate(['/finish']));
          this.dialog.loading(true);
        }
      });
  }

  setupPage(page: Page<Question>): void {

    this.r = undefined;
    this.c = {};
    this.page = page;

    if (!this.timer) {
      this.timer = setInterval(() => {
        this.updateTimer();
      }, 100);
    }

    if (page.content[0].multiple) {
      for (const s of page.content[0].choices) {
        this.c[s.label] = false;
      }
    }

    this.dialog.loading(false);
  }

  onClickReloadIcon(): void {
    this.getPage(this.page.currentNumber - 1);
  }

  updateTimer(): void {

    const millis = new Date().getTime() - new Date(this.page.content[0].startDatetime).getTime();
    const m = Math.floor(millis / (1000 * 60));
    this.m = ('00' + m).slice(-2);
    const s = Math.floor((millis - (m * 1000 * 60)) / 1000);
    this.s = ('00' + s).slice(-2);
  }
}
