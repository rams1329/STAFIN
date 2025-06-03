import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated()) {
      // If user is admin, redirect to admin dashboard
      if (this.authService.hasRole('ROLE_ADMIN')) {
        this.router.navigate(['/admin/dashboard']);
        return false;
      }
      
      // Allow access for non-admin users
      return true;
    }

    // If not authenticated, redirect to login
    this.router.navigate(['/login']);
    return false;
  }
} 