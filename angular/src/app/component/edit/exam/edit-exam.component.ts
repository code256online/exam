import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { modalParams } from 'src/environments/constants';
import { Exam } from 'src/app/model/exam';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-edit-exam',
  templateUrl: './edit-exam.component.html',
  styleUrls: ['./edit-exam.component.scss']
})
export class EditExamComponent implements OnInit {

  exam: Exam = new Exam();
  errors: { [key: string]: string[] } = {};
  readOnly: boolean;

  constructor(
    private editQuestionService: EditQuestionService,
    private dialog: DialogService,
    private errorHandler: DefaultErrorHandlerService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(x => {
      if (x['examNo']) {
        this.getInitialData(x['examNo']);
      }
    });
  }

  getInitialData(examNo: number): void {

    this.dialog.loading(true);
    this.editQuestionService.getExam(examNo)
      .subscribe(x => {
        this.exam = x;
        this.readOnly = true;
        this.dialog.loading(false);
      }, error => this.errorHandler.handle(error));
  }

  insert(): void {

    this.editQuestionService.validateExam(this.exam).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmCreateExam).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.editQuestionService.insertExam(this.exam)
                .then(() => this.router.navigate(['/edit/exam', this.exam.examNo]))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  update(): void {

    this.editQuestionService.validateExam(this.exam).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmEditExam).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              this.editQuestionService.updateExam(this.exam)
                .then(() => this.getInitialData(this.exam.examNo))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }
}
