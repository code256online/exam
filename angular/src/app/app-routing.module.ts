import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationGuard } from './guard/authentication.guard';

const routes: Routes = [
  { path: '', loadChildren: './component/question/question.module#QuestionModule' },
  {
    path: 'edit',
    canActivate: [AuthenticationGuard],
    canLoad: [AuthenticationGuard],
    loadChildren: './component/edit/edit.module#EditModule'
  },
  {
    path: 'history',
    canActivate: [AuthenticationGuard],
    canLoad: [AuthenticationGuard],
    loadChildren: './component/history/history.module#HistoryModule'
  },
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forRoot(routes, { useHash: true, onSameUrlNavigation: 'reload' }),
  ],
  exports: [
    RouterModule,
  ]
})
export class AppRoutingModule { }
