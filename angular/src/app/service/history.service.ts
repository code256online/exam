import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { ExamHistory } from '../model/exam-history';
import { Examinee } from '../model/examinee';
import { Page } from '../model/page';
import { QuestionMode } from '../model/question-mode';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  constructor(
    private http: HttpClient,
  ) { }

  getAllExaminees(mode: QuestionMode): Promise<Examinee[]> {
    return this.http.get<any>(`${environment.restBaseUri}/history/examinees?mode=${QuestionMode[mode]}`).pipe(map(x => x)).toPromise();
  }

  getHistoryPage(page: number, examineeId: number, questionMode: QuestionMode): Observable<Page<ExamHistory>> {

    const result = examineeId < 0
      ? this.getHistory(page, questionMode)
      : this.getExamineeHistory(page, examineeId, questionMode);

    return result.pipe(map(x => x));
  }

  getDetailPage(examineeId: number, examNo: number, examCoverage: number, examCount: number): Observable<ExamHistory> {

    // tslint:disable-next-line:max-line-length
    return this.http.get<any>(`${environment.restBaseUri}/history/detail?examineeId=${examineeId}&examNo=${examNo}&examCoverage=${examCoverage}&examCount=${examCount}`)
      .pipe(map(x => x.content[0]));
  }

  getFixedDetailPage(examineeId: number, fixedQuestionsId: number, examCount: number): Observable<ExamHistory> {

    // tslint:disable-next-line:max-line-length
    return this.http.get<any>(`${environment.restBaseUri}/history/fixedDetail?examineeId=${examineeId}&fixedQuestionsId=${fixedQuestionsId}&examCount=${examCount}`)
      .pipe(map(x => x.content[0]));
  }

  private getHistory(page: number, questionMode: QuestionMode): Observable<any> {
    return this.http.get<any>(`${environment.restBaseUri}/history?page=${page}&questionMode=${QuestionMode[questionMode]}`);
  }

  private getExamineeHistory(page: number, examineeId: number, questionMode: QuestionMode): Observable<any> {
    // tslint:disable-next-line:max-line-length
    return this.http.get<any>(`${environment.restBaseUri}/history?page=${page}&examineeId=${examineeId}&questionMode=${QuestionMode[questionMode]}`);
  }
}
