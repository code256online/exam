import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { faEdit, faMinusCircle, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { take } from 'rxjs/operators';
import { FixedQuestion } from 'src/app/model/fixed-question';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-question-init-fixed',
  templateUrl: './question-init-fixed.component.html',
  styleUrls: ['./question-init-fixed.component.scss']
})
export class QuestionInitFixedComponent implements OnInit {

  @Input()
  fixedQuestions: Promise<FixedQuestion[]>;
  @Input()
  errors: { [key: string]: string };
  @Input()
  formGroup: FormGroup;

  editIcon = faEdit;
  createIcon = faPlusCircle;
  deleteIcon = faMinusCircle;

  constructor(
    private authService: AuthenticationService,
    private router: Router,
    private dialog: DialogService,
    private editService: EditQuestionService,
  ) { }

  ngOnInit(): void { }

  onClickEditIcon(): void {
    this.dialog.modal(modalParams.warnEditFixed).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.router.navigate([
            '/edit/fixed',
            this.formGroup.controls['fixedQuestionsId'].value]);
        }
      });
  }

  onClickCreateIcon(): void {
    this.router.navigate(['/edit/fixed']);
  }

  onClickDeleteIcon(): void {
    this.dialog.modal(modalParams.warnDeleteFixed).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.editService.deleteFixedQuestion(
            this.formGroup.controls['fixedQuestionsId'].value);
        }
      });
  }

  get authenticated(): boolean {
    return this.authService.authenticated;
  }
}
