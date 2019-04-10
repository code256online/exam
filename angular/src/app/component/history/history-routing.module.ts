import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HistoryDetailComponent } from '../history/detail/history-detail.component';
import { HistoryComponent } from './history.component';
import { HistoryListComponent } from './list/history-list.component';

const routes: Routes = [
  {
    path: '', component: HistoryComponent, children: [
      { path: '', component: HistoryListComponent },
      { path: 'detail', component: HistoryDetailComponent },
    ]
  }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ],
  exports: [
    RouterModule,
  ]
})
export class HistoryRoutingModule { }
