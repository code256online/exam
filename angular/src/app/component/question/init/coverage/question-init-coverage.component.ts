import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { faEdit, faMinusCircle, faPlusCircle } from '@fortawesome/free-solid-svg-icons';
import { take } from 'rxjs/operators';
import { ExamCoverage } from 'src/app/model/exam-coverage';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { modalParams } from 'src/environments/constants';

@Component({
  selector: 'app-question-init-coverage',
  templateUrl: './question-init-coverage.component.html',
  styleUrls: ['./question-init-coverage.component.scss']
})
export class QuestionInitCoverageComponent implements OnInit {

  @Input()
  coverages: Promise<ExamCoverage[]>;
  @Input()
  errors: { [key: string]: string };

  @Input()
  formGroup: FormGroup;

  editIcon = faEdit;
  createIcon = faPlusCircle;
  deleteIcon = faMinusCircle;

  constructor(
    private dialog: DialogService,
    private authService: AuthenticationService,
    private router: Router,
    private editService: EditQuestionService,
  ) { }

  ngOnInit(): void { }

  onClickEditIcon(): void {

    if (this.formGroup.controls['examCoverage'].value === -1) {
      this.dialog.modal(modalParams.allCoverageCantEdit);
      return;
    }

    this.router.navigate([
      '/edit/coverage',
      this.formGroup.controls['examNo'].value,
      this.formGroup.controls['examCoverage'].value]);
  }

  onClickCreateIcon(): void {
    this.router.navigate(['/edit/coverage']);
  }

  onClickDeleteIcon(): void {

    if (this.formGroup.controls['examCoverage'].value === -1) {
      this.dialog.modal(modalParams.allCoverageCantDelete);
      return;
    }

    this.dialog.modal(modalParams.warnDeleteCoverage).pipe(take(1))
      .subscribe(ok => {
        if (ok) {
          this.editService.deleteCoverage(
            this.formGroup.controls['examNo'].value,
            this.formGroup.controls['examCoverage'].value);
        }
      });
  }

  get authenticated(): boolean {
    return this.authService.authenticated;
  }
}
