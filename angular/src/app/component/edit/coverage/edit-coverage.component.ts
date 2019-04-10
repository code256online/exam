import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { take } from 'rxjs/operators';
import { Exam } from 'src/app/model/exam';
import { ExamCoverage } from 'src/app/model/exam-coverage';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { InitService } from 'src/app/service/init.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-edit-coverage',
  templateUrl: './edit-coverage.component.html',
  styleUrls: ['./edit-coverage.component.scss']
})
export class EditCoverageComponent implements OnInit {

  coverage: ExamCoverage = new ExamCoverage();
  errors: { [key: string]: string[] } = {};
  readOnly: boolean;
  exams: Promise<Exam[]>;

  constructor(
    private editQuestionService: EditQuestionService,
    private initializeService: InitService,
    private dialog: DialogService,
    private errorHandler: DefaultErrorHandlerService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);
    this.exams = this.initializeService.getExams().then(x => {
      this.dialog.loading(false);
      return x;
    });
    this.route.params.subscribe(x => {
      if (x['id'] && x['examNo']) {
        this.getInitialData(x['id'], x['examNo']);
      }
    });
  }

  getInitialData(id: number, examNo: number): void {

    this.dialog.loading(true);
    this.editQuestionService.getCoverage(id, examNo)
      .subscribe(x => {
        this.coverage = x;
        this.readOnly = true;
        this.dialog.loading(false);
      }, error => this.errorHandler.handle(error));
  }

  insert(): void {

    this.editQuestionService.validateCoverage(this.coverage).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.createConfirmDialogParams).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.editQuestionService.insertCoverage(this.coverage)
                .then(() => this.router.navigate(['/edit/coverage', this.coverage.examNo, this.coverage.id]))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  update(): void {

    this.editQuestionService.validateCoverage(this.coverage).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.editCoverageConfirmParams).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.editQuestionService.updateCoverage(this.coverage)
                .then(() => this.getInitialData(this.coverage.examNo, this.coverage.id))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }
}
