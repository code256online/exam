import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ChartsModule } from 'ng2-charts';
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { LineChartComponent } from './line-chart/line-chart.component';
import { LoadingComponent } from './loading/loading.component';
import { LoginComponent } from './login/login.component';
import { ModalDialogComponent } from './modal-dialog/modal-dialog.component';

@NgModule({
    declarations: [
      LoadingComponent,
      ModalDialogComponent,
      ErrorDialogComponent,
      LineChartComponent,
      LoginComponent,
    ],
    imports: [
      CommonModule,
      ChartsModule,
      FormsModule,
      DragDropModule,
      ReactiveFormsModule,
    ],
    exports: [
      LoadingComponent,
      ModalDialogComponent,
      ErrorDialogComponent,
      LineChartComponent,
      LoginComponent,
    ]
})
export class CommonComponentModule { }
