import { PageItem } from './page-item';

export class Page<T> {

    content: T[];
    items: PageItem[];
    last: boolean;
    first: boolean;
    currentNumber: number;
    totalElements: number;
    totalPages: number;
    sizeList: number[];
}
