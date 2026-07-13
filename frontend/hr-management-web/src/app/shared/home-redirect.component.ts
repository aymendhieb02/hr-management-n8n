import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { homeUrlForUser } from '../core/utils/role-home.util';

@Component({
  selector: 'app-home-redirect',
  template: ''
})
export class HomeRedirectComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    void this.router.navigateByUrl(homeUrlForUser(this.authService.getCurrentUser()));
  }
}
