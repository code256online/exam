import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterContentChecked, Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { DialogService } from 'src/app/service/dialog.service';
import { HeaderMenuActiveLink } from '../../model/header-menu-active-link';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  animations: [
    trigger('openClose', [
      state('open', style({
        height: '150px',
        opacity: 1,
        display: 'block',
      })),
      state('closed', style({
        height: '0px',
        opacity: 0,
        display: 'none',
      })),
      transition('open => closed', [
        animate('300ms ease-in')
      ]),
      transition('closed => open', [
        animate('300ms ease-out')
      ]),
    ]),
  ],
})
export class HeaderComponent implements OnInit, AfterContentChecked {

  isOpen: boolean;
  displayLink: boolean;

  links = HeaderMenuActiveLink;
  activeLink: HeaderMenuActiveLink = HeaderMenuActiveLink.NORMAL;

  constructor(
    private router: Router,
    private authService: AuthenticationService,
    private dialog: DialogService,
  ) { }

  ngOnInit(): void { }

  ngAfterContentChecked(): void {

    this.router.events.subscribe(
      (event: any) => {
        if (event instanceof NavigationEnd) {
          if (this.router.url.includes('/history')) {
            this.activeLink = HeaderMenuActiveLink.HISTORY;
          } else if (this.router.url.includes('/edit')) {
            this.activeLink = HeaderMenuActiveLink.EDIT;
          } else {
            this.activeLink = HeaderMenuActiveLink.NORMAL;
          }
        }
      }
    );
  }

  toggleMenu(event: any): void {
    event.stopPropagation();
    this.isOpen = !this.isOpen;
  }

  isAuthenticated(): boolean {
    return this.authService.authenticated;
  }

  closeMenu(): void {
    this.isOpen = false;
  }

  login(): void {
    this.dialog.login({ show: true, redirectTo: '/' });
  }

  logout(): void {
    this.authService.logout();
  }

  onAnimationStart(): void {
    if (this.isOpen !== undefined) {
      this.displayLink = this.isOpen ? !this.displayLink : this.displayLink;
      console.log(this.displayLink);
    }
  }

  onAnimationEnd(): void {
    if (this.isOpen !== undefined) {
      this.displayLink = this.isOpen ? this.displayLink : !this.displayLink;
      console.log(this.displayLink);
    }
  }
}
