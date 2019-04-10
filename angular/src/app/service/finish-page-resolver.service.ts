import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { ExamHistory } from '../model/exam-history';
import { Page } from '../model/page';
import { QuestionService } from './question.service';

@Injectable({
  providedIn: 'root'
})
export class FinishPageResolverService implements Resolve<Page<ExamHistory>> {

  constructor(
    private questionService: QuestionService,
  ) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Page<ExamHistory>> {
    return this.questionService.getFinishPage();
  }
}
