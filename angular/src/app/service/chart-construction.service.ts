import { Injectable } from '@angular/core';
import { ChartData } from 'src/app/component/common/line-chart/line-chart.component';

@Injectable({
  providedIn: 'root'
})
export class ChartConstructionService {

  constructor() { }

  construct(values: number[], passingScores: number[], timestamps: string[], label: string): ChartData {

    const ret = new ChartData();
    ret.chartData.push({ data: values, label: label });
    ret.chartData.push({ data: passingScores, label: '合否ライン' });
    ret.chartLabels = timestamps;
    return ret;
  }
}
