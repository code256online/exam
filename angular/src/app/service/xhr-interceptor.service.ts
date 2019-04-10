import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class XhrInterceptorService implements HttpInterceptor {

  constructor(
    private cookieService: CookieService,
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const xsrfToken = this.cookieService.get('XSRF-TOKEN');
    const sessionId = this.cookieService.get('JSESSIONID');
    const xhr = req.clone({
      headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
        .set('X-XSRF-TOKEN', xsrfToken)
        .set('X-AUTH-TOKEN', sessionId),
      withCredentials: true,
    });
    return next.handle(xhr);
  }
}
