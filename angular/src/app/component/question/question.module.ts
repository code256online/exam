import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { LightboxModule } from 'ngx-lightbox';
import { FinishPageResolverService } from 'src/app/service/finish-page-resolver.service';
import { CommonComponentModule } from './../common/common-component.module';
import { QuestionFinishComponent } from './finish/question-finish.component';
import { QuestionInitCountComponent } from './init/count/question-init-count.component';
import { QuestionInitCoverageComponent } from './init/coverage/question-init-coverage.component';
import { QuestionInitExamsComponent } from './init/exams/question-init-exams.component';
import { QuestionInitFixedComponent } from './init/fixed/question-init-fixed.component';
import { QuestionInitComponent } from './init/question-init.component';
import { QuestionRoutingModule } from './question-routing.module';
import { QuestionComponent } from './question.component';
import { QuestionSetComponent } from './set/question-set.component';

@NgModule({
  declarations: [
    QuestionComponent,
    QuestionInitComponent,
    QuestionFinishComponent,
    QuestionSetComponent,
    QuestionInitExamsComponent,
    QuestionInitCoverageComponent,
    QuestionInitCountComponent,
    QuestionInitFixedComponent,
  ],
  imports: [
    QuestionRoutingModule,
    CommonModule,
    FormsModule,
    CommonComponentModule,
    LightboxModule,
    FontAwesomeModule,
    ReactiveFormsModule,
  ],
  providers: [
    FinishPageResolverService,
  ]
})
export class QuestionModule { }
