import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionFinishComponent } from './question-finish.component';

describe('FinishComponent', () => {
  let component: QuestionFinishComponent;
  let fixture: ComponentFixture<QuestionFinishComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionFinishComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionFinishComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
