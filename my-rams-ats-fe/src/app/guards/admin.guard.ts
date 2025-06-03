import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated() && this.authService.hasRole('ROLE_ADMIN')) {
      return true;
    }

    // If not admin, redirect to user dashboard or login
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/user/dashboard']);
    } else {
      this.router.navigate(['/login']);
    }
    return false;
  }
} 