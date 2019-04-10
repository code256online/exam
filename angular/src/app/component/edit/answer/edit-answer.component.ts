import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { faEdit, faMinusCircle, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { IAlbum, Lightbox } from 'ngx-lightbox';
import { take } from 'rxjs/operators';
import { Answer, UploadFile } from 'src/app/model/answer';
import { Exam } from 'src/app/model/exam';
import { ExamCoverage } from 'src/app/model/exam-coverage';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { InitService } from 'src/app/service/init.service';
import { modalParams } from 'src/environments/constants';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-edit-answer',
  templateUrl: './edit-answer.component.html',
  styleUrls: ['./edit-answer.component.scss']
})
export class EditAnswerComponent implements OnInit {

  answer: Answer = new Answer();
  errors: { [key: string]: string[] } = {};
  readOnly: boolean;
  exams: Promise<Exam[]>;
  coverages: Promise<ExamCoverage[]>;
  pictures: IAlbum[];

  editIcon = faEdit;
  createIcon = faPlusCircle;
  deleteIcon = faMinusCircle;

  constructor(
    private editQuestionService: EditQuestionService,
    private initializeService: InitService,
    private dialog: DialogService,
    private errorHandler: DefaultErrorHandlerService,
    private lightbox: Lightbox,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);
    this.answer.file = null;
    this.route.params.subscribe(params => {
      this.exams = this.initializeService.getExams().then(exams => {
        if (params['questionNo'] && params['examNo']) {
          this.readOnly = true;
          this.getInitialData(params['questionNo'], params['examNo']);
        } else {
          this.readOnly = false;
          this.answer.examNo = exams[0].examNo;
          this.editQuestionService.getMaxQuestionNo(exams[0].examNo).then(no => this.answer.questionNo = no + 1);
          this.coverages = this.initializeService.getCoveragesIncludeDeleted(exams[0].examNo).then(coverage => {
            this.dialog.loading(false);
            return coverage;
          });
        }
        return exams;
      });
    });
  }

  getInitialData(questionNo: number, examNo: number): void {

    this.dialog.loading(true);
    this.editQuestionService.getAnswer(examNo, questionNo)
      .subscribe(answer => {
        this.answer = answer;
        this.coverages = this.initializeService.getCoveragesIncludeDeleted(examNo)
          .then(coverage => {
            this.dialog.loading(false);
            return coverage;
          });
      }, error => this.errorHandler.handle(error));
  }

  onExamNoChange(): void {

    this.dialog.loading(true);
    if (!this.readOnly) {
      this.editQuestionService.getMaxQuestionNo(this.answer.examNo).then(x => this.answer.questionNo = x + 1);
    }
    this.coverages = this.initializeService.getCoveragesIncludeDeleted(this.answer.examNo).then(x => {
      this.dialog.loading(false);
      return x;
    });
  }

  onClickExamEditIcon(): void {
    this.dialog.modal(modalParams.confirmCancelToEditExam).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.router.navigate(['/edit/exam', this.answer.examNo]);
        }
      });
  }

  onClickExamCreateIcon(): void {
    this.dialog.modal(modalParams.confirmCancelToCreateExam).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.router.navigate(['/edit/exam']);
        }
      });
  }

  onClickExamDeleteIcon(): void {
    this.dialog.modal(modalParams.warnDeleteExam).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.dialog.loading(true);
          this.editQuestionService.deleteExam(this.answer.examNo).then(() => {
            this.exams = this.initializeService.getExams().then(x => {
              this.dialog.loading(false);
              return x;
            });
          });
        }
      });
  }

  onClickCoverageEditIcon(): void {

    if (this.answer.examCoverage === -1) {
      this.dialog.modal(modalParams.allCoverageCantEdit);
      return;
    }

    this.dialog.modal(modalParams.confirmCancelToEditCoverage).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.router.navigate(['/edit/coverage', this.answer.examNo, this.answer.examCoverage]);
        }
      });
  }

  onClickCoverageCreateIcon(): void {
    this.dialog.modal(modalParams.confirmCancelToCreateCoverage).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.router.navigate(['/edit/coverage']);
        }
      });
  }

  onClickCoverageDeleteIcon(): void {

    if (this.answer.examCoverage === -1) {
      this.dialog.modal(modalParams.allCoverageCantDelete);
      return;
    }

    this.dialog.modal(modalParams.warnDeleteCoverage).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.editQuestionService.deleteCoverage(this.answer.examNo, this.answer.examCoverage);
        }
      });
  }

  onChangeUploadFile(files: FileList) {

    if (files.length === 0) {
      this.answer.file = null;
    } else {
      const file = new UploadFile();
      file.name = files.item(0).name;
      const reader = new FileReader();
      reader.onload = function () {
        file.bytesByBase64 = btoa(this.result as string);
      };
      reader.readAsBinaryString(files.item(0));
      this.answer.file = file;
    }
  }

  updateConfirm(): void {

    if (this.answer.file.bytesByBase64) {
      this.dialog.modal(modalParams.warnImageUploading).pipe(take(1))
        .subscribe(ok => {
          if (ok) {
            this.update();
          }
        });
    } else {
      this.update();
    }
  }

  update(): void {

    this.errors = {};
    this.answer.insertMode = false;
    this.dialog.loading(true);
    this.editQuestionService.validateAnswer(this.answer).then(x => {
      this.dialog.loading(false);
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmEditAnswer).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.dialog.loading(true);
              this.editQuestionService.updateAnswer(this.answer).then(() => {
                this.dialog.loading(false);
                this.getInitialData(this.answer.questionNo, this.answer.examNo);
              }).catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  insert(): void {

    this.errors = {};
    this.answer.insertMode = true;
    this.dialog.loading(true);
    this.editQuestionService.validateAnswer(this.answer).then(x => {
      this.dialog.loading(false);
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmCreateAnswer).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.dialog.loading(true);
              this.editQuestionService.insertAnswer(this.answer).then(y => {
                this.dialog.loading(false);
                this.router.navigate(['/edit/answer', this.answer.examNo, this.answer.questionNo]);
              }).catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  delete(): void {

    this.dialog.modal(modalParams.warnDeleteAnswer).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.dialog.loading(true);
          this.editQuestionService.deleteAnswer(this.answer.examNo, this.answer.questionNo)
            .then(() => {
              this.dialog.loading(false);
              this.router.navigate(['/edit/list']);
            }).catch(error => this.errorHandler.handle(error));
        }
      });
  }

  openLightbox(index: number): void {

    this.pictures = [];
    if (this.answer.examNo && this.answer.questionNo) {
      // tslint:disable-next-line:max-line-length
      const src = `${environment.imageBaseUri}/E${('000' + this.answer.examNo).slice(-3)}Q${('000' + this.answer.questionNo).slice(-3)}.jpg`;
      this.pictures.push({
        src: src,
        thumb: src,
      });
      this.lightbox.open(this.pictures, index);
    }
  }

  closeLightbox(): void {
    this.lightbox.close();
  }
}
