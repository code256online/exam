import { AnswerListComponent } from './answer-list/answer-list.component';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EditComponent } from './edit.component';
import { EditExamComponent } from './exam/edit-exam.component';
import { EditFixedComponent } from './fixed/edit-fixed.component';
import { EditCoverageComponent } from './coverage/edit-coverage.component';
import { EditAnswerComponent } from './answer/edit-answer.component';

const routes: Routes = [
  {
    path: '', component: EditComponent, children: [
      { path: '', component: AnswerListComponent },
      { path: 'fixed', component: EditFixedComponent },
      { path: 'fixed/:id', component: EditFixedComponent },
      { path: 'exam', component: EditExamComponent },
      { path: 'exam/:examNo', component: EditExamComponent },
      { path: 'coverage', component: EditCoverageComponent },
      { path: 'coverage/:examNo/:id', component: EditCoverageComponent },
      { path: 'answer', component: EditAnswerComponent },
      { path: 'answer/:examNo/:questionNo', component: EditAnswerComponent },
    ]
  }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ],
  exports: [
    RouterModule,
  ]
})
export class EditRoutingModule { }
