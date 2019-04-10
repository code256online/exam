export class ExamHistory {

  examineeId: number;
  examineeName: string;
  examNo: number;
  examName: string;
  passingScore: number;
  examCoverage: number;
  examCoverageName: string;
  fixedQuestionsId: number;
  fixedQuestionsName: number;
  examCount: number;
  questionCount: number;
  answerCount: number;
  correctCount: number;
  answerRate: number;
  correctRate: number;
  incorrectQuestions: IncorrectQuestion[];
  correctAnswers: string[];
  durationMinutes: number;
  durationSeconds: number;
  timestamp: Date;

}

export class IncorrectQuestion {

  examNo: number;
  examName: string;
  questionNo: number;
}
