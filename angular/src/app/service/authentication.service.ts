import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  authenticated: boolean;

  constructor(
    private http: HttpClient,
    private router: Router,
  ) { }

  login(credentials: AuthenticationCredentials): Promise<boolean> {
    return this.http.post<boolean>(`${environment.restBaseUri}/auth/login`, credentials).toPromise()
      .then(() => this.authenticated = true);
  }

  logout(): void {
    this.http.post<void>(`${environment.restBaseUri}/auth/logout`, {}).toPromise()
      .then(() => this.authenticated = false)
      .finally(() => this.router.navigate(['/']));
  }

  authenticate(): Promise<boolean> {
    return this.http.get<any>(`${environment.restBaseUri}/auth/user`)
      .pipe(map(x => x && x.id > 0)).toPromise()
      .then(x => {
        this.authenticated = x;
        return x;
      }).catch(() => this.authenticated = false);
  }

  isAdmin(): Promise<boolean> {
    return this.http.get<any>(`${environment.restBaseUri}/auth/user`)
      .pipe(map(x => x && x.admin)).toPromise();
  }
}

export interface AuthenticationCredentials {

  username: string;
  password: string;
}
