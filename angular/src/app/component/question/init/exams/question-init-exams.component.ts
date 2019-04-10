import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { faEdit, faMinusCircle, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { take } from 'rxjs/operators';
import { Exam } from 'src/app/model/exam';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-question-init-exams',
  templateUrl: './question-init-exams.component.html',
  styleUrls: ['./question-init-exams.component.scss']
})
export class QuestionInitExamsComponent implements OnInit {

  @Input()
  exams: Promise<Exam[]>;
  @Input()
  errors: { [key: string]: string };

  @Output()
  examNoChange = new EventEmitter<number>();

  @Input()
  formGroup: FormGroup;

  editIcon = faEdit;
  createIcon = faPlusCircle;
  deleteIcon = faMinusCircle;

  constructor(
    private editService: EditQuestionService,
    private authService: AuthenticationService,
    private router: Router,
    private dialog: DialogService,
  ) { }

  ngOnInit(): void { }

  onExamNoChange(examNo: number): void {
    this.examNoChange.emit(examNo);
  }

  onClickEditIcon(): void {
    this.router.navigate(['/edit/exam', this.formGroup.controls['examNo'].value]);
  }

  onClickCreateIcon(): void {
    this.router.navigate(['/edit/exam']);
  }

  onClickDeleteIcon(): void {
    this.dialog.modal(modalParams.warnDeleteExam).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.editService.deleteExam(this.formGroup.controls['examNo'].value);
        }
      });
  }

  get authenticated(): boolean {
    return this.authService.authenticated;
  }
}
