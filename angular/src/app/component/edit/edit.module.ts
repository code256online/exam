import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { LightboxModule } from 'ngx-lightbox';
import { AnswerListComponent } from './answer-list/answer-list.component';
import { EditAnswerComponent } from './answer/edit-answer.component';
import { EditCoverageComponent } from './coverage/edit-coverage.component';
import { EditRoutingModule } from './edit-routing.module';
import { EditComponent } from './edit.component';
import { EditExamComponent } from './exam/edit-exam.component';
import { EditFixedComponent } from './fixed/edit-fixed.component';

@NgModule({
  declarations: [
    EditComponent,
    EditFixedComponent,
    EditExamComponent,
    EditCoverageComponent,
    EditAnswerComponent,
    AnswerListComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    EditRoutingModule,
    LightboxModule,
    FontAwesomeModule,
    DragDropModule,
  ]
})
export class EditModule { }
