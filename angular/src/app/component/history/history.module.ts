import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { LightboxModule } from 'ngx-lightbox';
import { CommonComponentModule } from '../common/common-component.module';
import { HistoryDetailComponent } from './detail/history-detail.component';
import { HistoryRoutingModule } from './history-routing.module';
import { HistoryComponent } from './history.component';
import { HistoryListComponent } from './list/history-list.component';

@NgModule({
    declarations: [
        HistoryComponent,
        HistoryDetailComponent,
        HistoryListComponent,
    ],
    imports: [
        HistoryRoutingModule,
        CommonComponentModule,
        CommonModule,
        FormsModule,
        LightboxModule,
        FontAwesomeModule,
    ]
})
export class HistoryModule { }
