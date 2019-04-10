import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionInitExamsComponent } from './question-init-exams.component';

describe('QuestionInitExamsComponent', () => {
  let component: QuestionInitExamsComponent;
  let fixture: ComponentFixture<QuestionInitExamsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionInitExamsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionInitExamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
