export class Question {

    examNo: number;
    questionNo: number;
    image: Image;
    choices: Choice[];
    multiple: boolean;
    startDatetime: string;
}

export class Image {

    src: string;
    alt: string;
    title: string;
}

export class Choice {
    label: string;
}
