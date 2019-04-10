import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FinishPageResolverService } from 'src/app/service/finish-page-resolver.service';
import { QuestionFinishComponent } from './finish/question-finish.component';
import { QuestionInitComponent } from './init/question-init.component';
import { QuestionComponent } from './question.component';
import { QuestionSetComponent } from './set/question-set.component';

const routes: Routes = [
  {
    path: '', component: QuestionComponent, children: [
      { path: '', component: QuestionInitComponent },
      { path: 'question', component: QuestionSetComponent },
      { path: 'finish', component: QuestionFinishComponent, resolve: { page: FinishPageResolverService } },
    ]
  },
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ],
  exports: [
    RouterModule,
  ],
  providers: [
    FinishPageResolverService
  ]
})
export class QuestionRoutingModule { }
