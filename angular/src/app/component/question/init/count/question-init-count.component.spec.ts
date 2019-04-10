import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionInitCountComponent } from './question-init-count.component';

describe('QuestionInitCountComponent', () => {
  let component: QuestionInitCountComponent;
  let fixture: ComponentFixture<QuestionInitCountComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionInitCountComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionInitCountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
