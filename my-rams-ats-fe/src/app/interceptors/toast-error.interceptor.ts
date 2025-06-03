import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastService } from '../services/toast.service';

@Injectable()
export class ToastErrorInterceptor implements HttpInterceptor {
  constructor(private toast: ToastService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Skip showing toast for certain URLs or conditions
        const skipToastFor = [
          '/api/auth/', // Skip for auth endpoints to handle manually
          '/api/users/profile' // Skip for profile endpoints to handle manually
        ];
        
        const shouldSkipToast = skipToastFor.some(url => request.url.includes(url));
        
        if (!shouldSkipToast) {
          let errorMsg = '';
          
          if (error.error instanceof ErrorEvent) {
            // Client-side error
            errorMsg = `Error: ${error.error.message}`;
          } else {
            // Server-side error
            errorMsg = error.error?.message || error.message || 'An unexpected error occurred';
            
            // Handle specific error codes with appropriate messages
            switch (error.status) {
              case 401:
                this.toast.error('Your session has expired. Please login again.', 'Authentication Error');
                break;
              case 403:
                this.toast.error('You do not have permission to perform this action.', 'Access Denied');
                break;
              case 404:
                this.toast.error('The requested resource was not found.', 'Not Found');
                break;
              case 422:
                this.toast.error('Please check your input and try again.', 'Validation Error');
                break;
              case 500:
                this.toast.error('Something went wrong on the server. Please try again later.', 'Server Error');
                break;
              case 502:
              case 503:
              case 504:
                this.toast.error('Service is temporarily unavailable. Please try again later.', 'Service Unavailable');
                break;
              default:
                if (error.status >= 400 && error.status < 500) {
                  this.toast.error(errorMsg, 'Request Error');
                } else if (error.status >= 500) {
                  this.toast.error('Server error occurred. Please try again later.', 'Server Error');
                } else {
                  this.toast.error(errorMsg, 'Error');
                }
                break;
            }
          }
        }
        
        return throwError(() => error);
      })
    );
  }
} 