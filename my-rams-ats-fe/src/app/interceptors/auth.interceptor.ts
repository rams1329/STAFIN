import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Get the auth token from the service
    const authToken = this.authService.getToken();

    // Only add Authorization header to API calls (excluding login)
    let authRequest = request;
    if (
      authToken &&
      request.url.includes('/api/') &&
      !request.url.includes('/api/auth/login')
    ) {
      authRequest = request.clone({
        headers: request.headers.set('Authorization', `Bearer ${authToken}`),
      });
      
      // Debug logging for profile API requests
      if (request.url.includes('/api/user/profile')) {
        console.debug('🔐 Adding auth header to profile request:', request.url);
        console.debug('🔑 Token (first 20 chars):', authToken.substring(0, 20) + '...');
      }
    }

    return next.handle(authRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        // Enhanced error handling for profile endpoints
        if (error.status === 401 && request.url.includes('/api/user/profile')) {
          console.error('❌ 401 Unauthorized on profile endpoint:', request.url);
          console.error('🔍 Request details:', {
            method: request.method,
            url: request.url,
            hasAuthHeader: !!authRequest.headers.get('Authorization'),
            tokenPresent: !!authToken
          });
        }
        
        // Handle 401 Unauthorized globally, except for login endpoint
        if (
          error.status === 401 &&
          !request.url.includes('/api/auth/login')
        ) {
          this.authService.logout();
          if (!this.router.url.includes('/login')) {
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: this.router.url },
            });
          }
        }
        // Propagate the error
        return throwError(() => error);
      })
    );
  }
}
