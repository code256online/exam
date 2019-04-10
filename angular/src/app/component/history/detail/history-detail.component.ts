import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { IAlbum, Lightbox } from 'ngx-lightbox';
import { ExamHistory } from 'src/app/model/exam-history';
import { QuestionMode } from 'src/app/model/question-mode';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { HistoryService } from 'src/app/service/history.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-history-detail',
  templateUrl: './history-detail.component.html',
  styleUrls: ['./history-detail.component.scss']
})
export class HistoryDetailComponent implements OnInit {

  page: ExamHistory = new ExamHistory();
  pictures: IAlbum[] = [];
  questionMode: QuestionMode = QuestionMode.NORMAL;
  mode = QuestionMode;

  constructor(
    private route: ActivatedRoute,
    private historyService: HistoryService,
    private lightbox: Lightbox,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);

    this.route.paramMap.subscribe(x => {
      const fixedQuestionsId = this.route.snapshot.queryParamMap.get('fixedQuestionsId');
      if (fixedQuestionsId) {
        this.questionMode = QuestionMode.FIXED;
        this.getFixedDetailPage();
      } else {
        this.getNormalDetailPage();
      }
    });
  }

  getNormalDetailPage(): void {

    this.historyService.getDetailPage(
      parseInt(this.route.snapshot.queryParamMap.get('examineeId'), 10),
      parseInt(this.route.snapshot.queryParamMap.get('examNo'), 10),
      parseInt(this.route.snapshot.queryParamMap.get('examCoverage'), 10),
      parseInt(this.route.snapshot.queryParamMap.get('examCount'), 10)
    ).subscribe(y => {
      this.page = y;
      this.setupLightbox(y);
      this.dialog.loading(false);
    },
    error => this.errorHandler.handle(error));
  }

  getFixedDetailPage(): void {

    this.historyService.getFixedDetailPage(
      parseInt(this.route.snapshot.queryParamMap.get('examineeId'), 10),
      parseInt(this.route.snapshot.queryParamMap.get('fixedQuestionsId'), 10),
      parseInt(this.route.snapshot.queryParamMap.get('examCount'), 10)
    ).subscribe(y => {
      this.page = y;
      this.setupLightbox(y);
      this.dialog.loading(false);
    },
    error => this.errorHandler.handle(error));
  }

  setupLightbox(page: ExamHistory): void {

    for (const qn of page.incorrectQuestions) {
      const src = `${environment.imageBaseUri}/E${('000' + qn.examNo).slice(-3)}Q${('000' + qn.questionNo).slice(-3)}.jpg`;
      this.pictures.push({
        src: src,
        thumb: src,
      });
    }
  }

  openLightbox(index: number): void {
    this.lightbox.open(this.pictures, index);
  }

  closeLightbox(): void {
    this.lightbox.close();
  }
}
