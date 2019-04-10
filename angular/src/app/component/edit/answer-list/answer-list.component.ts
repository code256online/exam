import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Answer } from 'src/app/model/answer';
import { Exam } from 'src/app/model/exam';
import { Page } from 'src/app/model/page';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { InitService } from 'src/app/service/init.service';

@Component({
  selector: 'app-answer-list',
  templateUrl: './answer-list.component.html',
  styleUrls: ['./answer-list.component.scss']
})
export class AnswerListComponent implements OnInit {

  examNo: number;
  page: Page<Answer>;

  exams: Promise<Exam[]>;

  constructor(
    private initService: InitService,
    private editService: EditQuestionService,
    private dialog: DialogService,
    private router: Router,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);
    this.exams = this.initService.getExams().then(x => {
      this.dialog.loading(false);
      return x;
    });
  }

  onExamNoChange(examNo: number): void {
    this.examNo = examNo;
    this.loadPage(0);
  }

  loadPage(page: number): void {

    this.dialog.loading(true);
    this.editService.getAnswerPage(this.examNo, page).subscribe(x => {
      this.page = x;
      this.dialog.loading(false);
    });
  }

  createNew(): void {
    this.router.navigate(['edit/answer']);
  }

  toEdit(answer: Answer): void {
    this.router.navigate(['/edit/answer', answer.examNo, answer.questionNo]);
  }
}
