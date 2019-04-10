import { FormGroup } from '@angular/forms';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-question-init-count',
  templateUrl: './question-init-count.component.html',
  styleUrls: ['./question-init-count.component.scss']
})
export class QuestionInitCountComponent implements OnInit {

  @Input()
  errors: { [key: string]: string };
  @Input()
  formGroup: FormGroup;

  constructor() { }

  ngOnInit(): void { }
}
