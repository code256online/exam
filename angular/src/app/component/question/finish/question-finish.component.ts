import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { IAlbum, Lightbox } from 'ngx-lightbox';
import { ChartData } from 'src/app/component/common/line-chart/line-chart.component';
import { ExamHistory } from 'src/app/model/exam-history';
import { Page } from 'src/app/model/page';
import { ChartConstructionService } from 'src/app/service/chart-construction.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-question-finish',
  templateUrl: './question-finish.component.html',
  styleUrls: ['./question-finish.component.scss']
})
export class QuestionFinishComponent implements OnInit {

  page: Page<ExamHistory>;
  fixed: boolean;
  pictures: IAlbum[] = [];
  chartData: ChartData;

  constructor(
    private route: ActivatedRoute,
    private lightbox: Lightbox,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
    private chartService: ChartConstructionService,
  ) { }

  ngOnInit() {
    this.dialog.loading(true);
    this.route.data.subscribe(x => {
        this.page = x['page'];
        this.fixed = x['page'].content[0].fixedQuestionsId > 0;
        for (const qn of x['page'].content[0].incorrectQuestions) {
          const src = `${environment.imageBaseUri}/E${('000' + qn.examNo).slice(-3)}Q${('000' + qn.questionNo).slice(-3)}.jpg`;
          this.pictures.push({
            src: src,
            thumb: src,
          });
        }
        if (!this.fixed) {
          this.chartData = this.chartService.construct(
            x['page'].content.slice().reverse().map(y => y.correctRate),
            x['page'].content.slice().reverse().map(y => y.passingScore),
            x['page'].content.slice().reverse().map(y => y.timestamp.toLocaleString()),
            '正答率'
          );
        }
        this.dialog.loading(false);
      },
      error => {
        this.errorHandler.handle(error);
        this.dialog.loading(false);
      });
  }

  openLightbox(index: number) {
    this.lightbox.open(this.pictures, index);
  }

  closeLightbox(): void {
    this.lightbox.close();
  }
}
