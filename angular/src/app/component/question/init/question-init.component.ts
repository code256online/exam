import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { take } from 'rxjs/operators';
import { Exam } from 'src/app/model/exam';
import { ExamCoverage } from 'src/app/model/exam-coverage';
import { FixedQuestion } from 'src/app/model/fixed-question';
import { InitData } from 'src/app/model/init-data';
import { QuestionMode } from 'src/app/model/question-mode';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { InitDataBindService } from 'src/app/service/init-data-bind.service';
import { InitService } from 'src/app/service/init.service';
import { QuestionService } from 'src/app/service/question.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-question-init',
  templateUrl: './question-init.component.html',
  styleUrls: ['./question-init.component.scss'],
})
export class QuestionInitComponent implements OnInit {

  initForm: FormGroup = new FormGroup({
    examNo: new FormControl(undefined),
    examCoverage: new FormControl(undefined),
    fixedQuestionsId: new FormControl(undefined),
    questionCount: new FormControl(undefined, [Validators.min(1), Validators.max(999)])
  });

  mode = QuestionMode;
  questionMode: QuestionMode = QuestionMode.NORMAL;

  errors: { [key: string]: string[] } = {};

  exams: Promise<Exam[]>;
  coverages: Promise<ExamCoverage[]>;
  fixedQuestions: Promise<FixedQuestion[]>;

  constructor(
    private initializeService: InitService,
    private questionService: QuestionService,
    private router: Router,
    private errorHandler: DefaultErrorHandlerService,
    private dialog: DialogService,
    private dataBinder: InitDataBindService,
    private authenticationService: AuthenticationService,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);
    this.resumeCheck();

    this.exams = this.initializeService.getExams()
      .then(x => {
        this.coverages = this.initializeService.getCoverages(x[0].examNo)
          .then(y => {
            this.initForm.controls['examCoverage'].setValue(y[0].id);
            return y;
          }).finally(() => this.dialog.loading(false));

        this.initForm.controls['examNo'].setValue(x[0].examNo);
        return x;
      });
  }

  onQuestionModeClick(): void {

    this.dialog.loading(true);
    if (this.questionMode === QuestionMode.NORMAL) {
      this.fixedQuestions = this.initializeService.getFixedQuestions()
        .then(x => {
          const array = x.reverse();
          this.questionMode = QuestionMode.FIXED;
          this.initForm.controls['fixedQuestionsId'].setValue(array[0].id);
          return array;
        }).finally(() => this.dialog.loading(false));
    } else {
      this.coverages = this.initializeService.getCoverages(this.initForm.controls['examNo'].value)
        .then(x => {
          this.questionMode = QuestionMode.NORMAL;
          this.initForm.controls['examCoverage'].setValue(x[0].id);
          return x;
        }).finally(() => this.dialog.loading(false));
    }
  }

  onExamNoChange(examNo: number): void {

    this.dialog.loading(true);
    if (this.questionMode === QuestionMode.NORMAL) {
      this.coverages = this.initializeService.getCoverages(examNo)
        .then(x => {
          this.initForm.controls['examCoverage'].setValue(x[0].id);
          return x;
        }).finally(() => this.dialog.loading(false));
    }
  }

  resumeCheck(): void {

    this.authenticationService.authenticate().then(login => {
      if (login) {
        this.questionService.resumeCheck().then(resume => {
          if (resume) {
            this.dialog.modal(modalParams.confirmResume).pipe(take(1))
              .subscribe(ok => {
                if (ok) {
                  this.router.navigate(['/question']);
                } else {
                  this.questionService.reset();
                }
              });
          }
        }, error => this.errorHandler.handle(error));
      }
    });
  }

  initQuestion(): void {

    const initData: InitData = {
      examNo: this.initForm.controls['examNo'].value,
      examCoverage: this.initForm.controls['examCoverage'].value,
      questionCount: this.initForm.controls['questionCount'].value,
      fixedQuestionsId: this.initForm.controls['fixedQuestionsId'].value,
      questionMode: this.questionMode
    };
    this.dataBinder.initData = initData;
    this.questionService.initValidation(initData).then(page => {

      if (Object.keys(page).length !== 0) {
        this.errors = page;
      } else {
        this.dialog.modal(modalParams.confirmStart).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.router.navigate(['/question']);
            }
          });
      }
    }, error => {
      if (error.status === 403) {
        this.dialog.login({ show: true, callback: () => this.initQuestion() });
      } else {
        this.errorHandler.handle(error);
      }
    });
  }
}
