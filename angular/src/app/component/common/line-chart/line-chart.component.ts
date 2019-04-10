import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent implements OnInit {

  @Input()
  visible: boolean;
  @Input()
  chartData: ChartData;

  chartOptions = {
    scales: {
      yAxes: [{
        ticks: {
          min: 0,
          max: 100,
        }
      }]
    }
  };

  constructor() { }

  ngOnInit() { }

}
export class ChartData {

  chartType = 'line';
  chartLabels = [];
  chartData = [];
}
