import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionInitCoverageComponent } from './question-init-coverage.component';

describe('QuestionInitCoverageComponent', () => {
  let component: QuestionInitCoverageComponent;
  let fixture: ComponentFixture<QuestionInitCoverageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionInitCoverageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionInitCoverageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
