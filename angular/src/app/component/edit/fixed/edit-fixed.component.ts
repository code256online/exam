import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { faEraser, faGripVertical } from '@fortawesome/free-solid-svg-icons';
import { IAlbum, Lightbox } from 'ngx-lightbox';
import { take } from 'rxjs/operators';
import { Exam } from 'src/app/model/exam';
import { FixedQuestion, FixedQuestionData } from 'src/app/model/fixed-question';
import { DefaultErrorHandlerService } from 'src/app/service/default-error-handler.service';
import { DialogService } from 'src/app/service/dialog.service';
import { EditQuestionService } from 'src/app/service/edit-question.service';
import { InitService } from 'src/app/service/init.service';
import { modalParams } from 'src/environments/constants';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-edit-fixed',
  templateUrl: './edit-fixed.component.html',
  styleUrls: ['./edit-fixed.component.scss']
})
export class EditFixedComponent implements OnInit {

  exams: Promise<Exam[]>;

  examNo: number;
  examName: string;
  questionNo: number;

  fixedQuestion: FixedQuestion = new FixedQuestion();
  errors: { [key: string]: string[] } = {};
  readOnly: boolean;
  pictures: IAlbum[] = [];

  gripIcon = faGripVertical;
  deleteIcon = faEraser;

  constructor(
    private initService: InitService,
    private editQuestionService: EditQuestionService,
    private dialog: DialogService,
    private errorHandler: DefaultErrorHandlerService,
    private lightbox: Lightbox,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {

    this.dialog.loading(true);
    this.exams = this.initService.getExams().then(x => {
      this.route.params.subscribe(y => {
        if (y['id']) {
          this.getInitialData(y['id']);
        } else {
          this.fixedQuestion.questions = [];
          this.dialog.loading(false);
        }
      });
      return x;
    });
  }

  getInitialData(id: number): void {

    this.dialog.loading(true);
    this.editQuestionService.getFixedQuestion(id)
      .subscribe(x => {
        this.fixedQuestion = x;
        this.readOnly = true;
        this.resolvePictures();
        this.dialog.loading(false);
      }, error => this.errorHandler.handle(error));
  }

  onChangeExamNo(name: string): void {
    this.examName = name;
  }

  appendQuestion(): void {
    this.fixedQuestion.questions.push({ examNo: this.examNo, examName: this.examName, questionNo: this.questionNo });
    this.resolvePictures();
    this.questionNo = undefined;
  }

  insert(): void {

    console.log(new Date().toLocaleTimeString() + ' insert validate');
    this.editQuestionService.validateFixedQuestion(this.fixedQuestion).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmCreateFixed).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              console.log(new Date().toLocaleTimeString() + ' insert!');
              this.editQuestionService.insertFixedQuestion(this.fixedQuestion)
                .then(id => this.router.navigate(['/edit/fixed', id]))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  update(): void {

    console.log(new Date().toLocaleTimeString() + ' update validate');
    this.editQuestionService.validateFixedQuestion(this.fixedQuestion).then(x => {
      if (Object.keys(x).length > 0) {
        this.errors = x;
      } else {
        this.dialog.modal(modalParams.confirmEditFixed).pipe(take(1))
          .subscribe(ok => {
            if (ok) {
              console.log(new Date().toLocaleTimeString() + ' update!');
              this.editQuestionService.updateFixedQuestion(this.fixedQuestion)
                .then(() => this.getInitialData(this.fixedQuestion.id))
                .catch(error => this.errorHandler.handle(error));
            }
          });
      }
    });
  }

  onQuestionListDrop(event: CdkDragDrop<FixedQuestionData>): void {
    moveItemInArray(this.fixedQuestion.questions, event.previousIndex, event.currentIndex);
    this.resolvePictures();
  }

  resolvePictures(): void {
    this.pictures = [];
    for (const question of this.fixedQuestion.questions) {
      const uri = `${environment.imageBaseUri}/E${('000' + question.examNo).slice(-3)}Q${('000' + question.questionNo).slice(-3)}.jpg`;
      this.pictures.push({ src: uri, thumb: uri });
    }
  }

  openLightbox(index: number): void {
    if (this.pictures.length > 0) {
      this.lightbox.open(this.pictures, index);
    }
  }

  closeLightbox(): void {
    this.lightbox.close();
  }

  closeQuestionsError(index: number): void {
    this.errors['questions'].splice(index, 1);
  }

  removeQuestion(index: number): void {
    this.fixedQuestion.questions.splice(index, 1);
  }

  get canAppend(): boolean {
    if (this.examNo && this.questionNo) {
      return true;
    }
    return false;
  }
}
