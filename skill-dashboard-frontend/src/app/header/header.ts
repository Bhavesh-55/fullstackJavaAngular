import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../services/auth';

@Component({
  selector: 'app-header',
  imports: [RouterLink,RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser = this.authService.currentUser;

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}