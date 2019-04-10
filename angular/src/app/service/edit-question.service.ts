import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Answer } from '../model/answer';
import { Exam } from '../model/exam';
import { ExamCoverage } from '../model/exam-coverage';
import { FixedQuestion } from '../model/fixed-question';
import { Page } from '../model/page';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EditQuestionService {

  constructor(
    private http: HttpClient,
  ) { }

  getFixedQuestion(id: number): Observable<FixedQuestion> {
    return this.http.get<FixedQuestion>(`${environment.restBaseUri}/fixed/${id}`);
  }

  validateFixedQuestion(fixedQuestion: FixedQuestion): Promise<{ [key: string]: string[] }> {
    return this.http.post<{ [key: string]: string[] }>(`${environment.restBaseUri}/fixed/validate`, fixedQuestion).toPromise();
  }

  insertFixedQuestion(fixedQuestion: FixedQuestion): Promise<number> {
    return this.http.put<number>(`${environment.restBaseUri}/fixed`, fixedQuestion).toPromise();
  }

  updateFixedQuestion(fixedQuestion: FixedQuestion): Promise<void> {
    return this.http.post<void>(`${environment.restBaseUri}/fixed`, fixedQuestion).toPromise();
  }

  deleteFixedQuestion(id: number): Promise<void> {
    return this.http.delete<void>(`${environment.restBaseUri}/fixed/${id}`).toPromise();
  }

  getExam(examNo: number): Observable<Exam> {
    return this.http.get<Exam>(`${environment.restBaseUri}/exam/${examNo}`);
  }

  validateExam(exam: Exam): Promise<{ [key: string]: string[] }> {
    return this.http.post<{ [key: string]: string[] }>(`${environment.restBaseUri}/exam/validate`, exam).toPromise();
  }

  insertExam(exam: Exam): Promise<void> {
    return this.http.put<void>(`${environment.restBaseUri}/exam`, exam).toPromise();
  }

  updateExam(exam: Exam): Promise<void> {
    return this.http.post<void>(`${environment.restBaseUri}/exam`, exam).toPromise();
  }

  deleteExam(examNo: number): Promise<void> {
    return this.http.delete<void>(`${environment.restBaseUri}/exam/${examNo}`).toPromise();
  }

  getCoverage(id: number, examNo: number): Observable<ExamCoverage> {
    return this.http.get<ExamCoverage>(`${environment.restBaseUri}/coverage/${examNo}/${id}`);
  }

  validateCoverage(coverage: ExamCoverage): Promise<{ [key: string]: string[] }> {
    return this.http.post<{ [key: string]: string[] }>(`${environment.restBaseUri}/coverage/validate`, coverage).toPromise();
  }

  insertCoverage(coverage: ExamCoverage): Promise<void> {
    return this.http.put<void>(`${environment.restBaseUri}/coverage`, coverage).toPromise();
  }

  updateCoverage(coverage: ExamCoverage): Promise<void> {
    return this.http.post<void>(`${environment.restBaseUri}/coverage`, coverage).toPromise();
  }

  deleteCoverage(examNo: number, id: number): Promise<void> {
    return this.http.delete<void>(`${environment.restBaseUri}/coverage/${examNo}/${id}`).toPromise();
  }

  getAnswerPage(examNo: number, page: number): Observable<Page<Answer>> {
    return this.http.get<any>(`${environment.restBaseUri}/answer?examNo=${examNo}&page=${page}`).pipe(x => x);
  }

  getAnswer(examNo: number, questionNo: number): Observable<Answer> {
    return this.http.get<Answer>(`${environment.restBaseUri}/answer/${examNo}/${questionNo}`);
  }

  getMaxQuestionNo(examNo: number): Promise<number> {
    return this.http.get<number>(`${environment.restBaseUri}/answer/maxQuestionNo/${examNo}`).toPromise();
  }

  validateAnswer(answer: Answer): Promise<{ [key: string]: string[] }> {
    return this.http.post<{ [key: string]: string[] }>(`${environment.restBaseUri}/answer/validate`, answer).toPromise();
  }

  insertAnswer(answer: Answer): Promise<void> {
    return this.http.put<void>(`${environment.restBaseUri}/answer`, answer).toPromise();
  }

  updateAnswer(answer: Answer): Promise<void> {
    return this.http.post<void>(`${environment.restBaseUri}/answer`, answer).toPromise();
  }

  deleteAnswer(examNo: number, questionNo: number): Promise<void> {
    return this.http.delete<void>(`${environment.restBaseUri}/answer/${examNo}/${questionNo}`).toPromise();
  }
}
