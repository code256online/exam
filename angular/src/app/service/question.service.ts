import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { ExamHistory } from '../model/exam-history';
import { InitData } from '../model/init-data';
import { Page } from '../model/page';
import { Question } from '../model/question';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {

  constructor(
    private http: HttpClient,
  ) { }

  resumeCheck(): Promise<boolean> {
    return this.http.get<boolean>(`${environment.restBaseUri}/question/resumeCheck`).toPromise();
  }

  reset(): void {
    this.http.get<void>(`${environment.restBaseUri}/question/reset`).subscribe();
  }

  initValidation(initData: InitData): Promise<{ [key: string]: string[] }> {
    return this.http.post<{[key: string]: string[]}>(`${environment.restBaseUri}/question/initValidation`, initData).toPromise();
  }

  initialize(initData: InitData): Observable<Page<Question>> {
    return this.http.post<any>(`${environment.restBaseUri}/question/init`, initData).pipe(map(x => x));
  }

  getQuestionPage(page: number, c: { [key: string]: boolean }, r: string): Observable<Page<Question>> {
    return this.http.get<any>(this.buildQuery(`${environment.restBaseUri}/question`, page, c, r)).pipe(map(x => x));
  }

  finish(c: { [key: string]: boolean }, r: string): Promise<void> {
    return this.http.get<void>(this.buildQuery(`${environment.restBaseUri}/question/finish`, null, c, r)).toPromise();
  }

  getFinishPage(): Observable<Page<ExamHistory>> {
    return this.http.get<any>(`${environment.restBaseUri}/question/finishPage`).pipe(map(x => x));
  }

  private buildQuery(base: string, page: number, c: { [key: string]: boolean }, r: string): string {

    base += '?';

    if (page !== null) {
      base += `page=${page}`;
    }

    if (Object.keys(c).length) {
      for (let i = 0; i < Object.keys(c).length; i++) {
        if (c[Object.keys(c)[i]]) {
          base += `&c=${Object.keys(c)[i]}`;
        }
      }
    } else if (r) {
      base += `&r=${r}`;
    }

    return base.replace('?&', '?');
  }
}
