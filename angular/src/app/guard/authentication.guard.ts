import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanLoad, Route, RouterStateSnapshot, UrlSegment } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DialogService } from 'src/app/service/dialog.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate, CanLoad {

  constructor(
    private authService: AuthenticationService,
    private dialog: DialogService,
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {

    return this.authService.authenticate().finally(() => {
      if (!this.authService.authenticated) {
        this.dialog.login({ show: true, redirectTo: state.url });
      }
    });
  }

  canLoad(route: Route, segments: UrlSegment[]): Promise<boolean> {

    return this.authService.authenticate().finally(() => {
      if (!this.authService.authenticated) {
        this.dialog.login({ show: true, redirectTo: route.path });
      }
    });
  }
}
