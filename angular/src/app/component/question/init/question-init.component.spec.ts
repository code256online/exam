import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionInitComponent } from './question-init.component';

describe('QuestionInitComponent', () => {
  let component: QuestionInitComponent;
  let fixture: ComponentFixture<QuestionInitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionInitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionInitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
