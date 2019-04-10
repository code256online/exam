import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { CookieService } from 'ngx-cookie-service';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CommonComponentModule } from './component/common/common-component.module';
import { HeaderComponent } from './component/header/header.component';
import { HeaderModule } from './component/header/header.module';
import { AuthenticationGuard } from './guard/authentication.guard';
import { DefaultErrorHandlerService } from './service/default-error-handler.service';
import { XhrInterceptorService } from './service/xhr-interceptor.service';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    CommonComponentModule,
    HeaderModule,
    AppRoutingModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: XhrInterceptorService, multi: true },
    CookieService,
    DefaultErrorHandlerService,
    AuthenticationGuard,
  ],
  exports: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
