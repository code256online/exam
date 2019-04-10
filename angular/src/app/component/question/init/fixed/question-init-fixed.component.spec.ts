import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionInitFixedComponent } from './question-init-fixed.component';

describe('FixedComponent', () => {
  let component: QuestionInitFixedComponent;
  let fixture: ComponentFixture<QuestionInitFixedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionInitFixedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionInitFixedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
