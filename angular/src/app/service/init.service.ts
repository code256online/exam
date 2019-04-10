import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Exam } from '../model/exam';
import { ExamCoverage } from '../model/exam-coverage';
import { Examinee } from '../model/examinee';
import { FixedQuestion } from '../model/fixed-question';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class InitService {

  constructor(
    private http: HttpClient,
  ) { }

  getExaminees(): Promise<Examinee[]> {
    return this.http.get<Examinee[]>(`${environment.restBaseUri}/examinee`).toPromise();
  }

  getExams(): Promise<Exam[]> {
    return this.http.get<Exam[]>(`${environment.restBaseUri}/exam`).toPromise();
  }

  getCoverages(examNo: number): Promise<ExamCoverage[]> {
    return this.http.get<ExamCoverage[]>(`${environment.restBaseUri}/coverage/${examNo}`).toPromise();
  }

  getCoveragesIncludeDeleted(examNo: number): Promise<ExamCoverage[]> {
    return this.http.get<ExamCoverage[]>(`${environment.restBaseUri}/coverage/includeDeleted/${examNo}`).toPromise();
  }

  getFixedQuestions(): Promise<FixedQuestion[]> {
    return this.http.get<FixedQuestion[]>(`${environment.restBaseUri}/fixed`).toPromise();
  }
}
