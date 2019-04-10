export class Answer {

    examNo: number;
    examName: string;
    questionNo: number;
    examCoverage: number;
    examCoverageName: string;
    choicesCount: number;
    correctAnswers: string;
    modifiedAt: Date;
    file: UploadFile;
    insertMode: boolean;
}
export class UploadFile {

    name: string;
    bytesByBase64: string;
}
