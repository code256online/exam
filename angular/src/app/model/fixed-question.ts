export class FixedQuestion {

  id: number;
  name: string;
  questions: FixedQuestionData[];
  modifiedAt: Date;
}

export class FixedQuestionData {

  examNo: number;
  examName: string;
  questionNo: number;
}
