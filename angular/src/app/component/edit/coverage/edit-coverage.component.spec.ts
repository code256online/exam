import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCoverageComponent } from './edit-coverage.component';

describe('EditCoverageComponent', () => {
  let component: EditCoverageComponent;
  let fixture: ComponentFixture<EditCoverageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditCoverageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCoverageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
