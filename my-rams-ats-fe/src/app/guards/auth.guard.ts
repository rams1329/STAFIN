import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    
    console.log('🛡️ AuthGuard checking access to:', state.url);
    const isAuthenticated = this.authService.isAuthenticated();
    console.log('🔐 User authenticated?', isAuthenticated);
    
    if (isAuthenticated) {
      console.log('✅ User is authenticated, checking roles...');
      // Check if route requires specific roles
      const requiredRoles = route.data?.['roles'] as string[];
      if (requiredRoles && requiredRoles.length > 0) {
        console.log('🎭 Required roles:', requiredRoles);
        if (this.authService.hasAnyRole(requiredRoles)) {
          console.log('✅ User has required roles, access granted');
          return true;
        } else {
          console.log('❌ User does not have required roles, redirecting to unauthorized');
          // User doesn't have required roles, redirect to unauthorized page
          this.router.navigate(['/unauthorized']);
          return false;
        }
      }
      console.log('✅ No specific roles required, access granted');
      return true;
    }

    console.log('❌ User not authenticated, redirecting to login');
    // Not authenticated, redirect to login
    this.router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url }
    });
    return false;
  }
} 