import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError, timeout } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ContactMessage } from '../models/user.model';
import { AuthService } from './auth.service';

// Export ContactMessage for use in components
export type { ContactMessage } from '../models/user.model';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private baseUrl = `${environment.apiUrl}/api/contact`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found for contact service');
      this.authService.logout();
      return throwError(() => new Error('Authentication required'));
    }
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }

  // Enhanced method with comprehensive filters and pagination
  getMessagesWithFilters(params: any): Observable<any> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }

    let httpParams = new HttpParams();
    
    // Add all parameters to HttpParams
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    console.log('🚀 Making contact messages API call to:', `${this.baseUrl}/messages`);
    console.log('📋 With params:', httpParams.toString());

    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/messages`, { 
      ...options, 
      params: httpParams 
    }).pipe(
      map(response => {
        console.log('🔍 Contact messages API response:', response);
        // Return the data from ApiResponse
        return response.data || response;
      }),
      timeout(10000),
      catchError(this.handleError)
    );
  }

  getMessages(): Observable<ContactMessage[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<ContactMessage[]>>(`${this.baseUrl}/messages`, options)
      .pipe(
        map(response => response.data || response as any),
        timeout(10000),
        catchError(this.handleError)
      );
  }

  getUnreadMessages(): Observable<ContactMessage[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<ContactMessage[]>>(`${this.baseUrl}/messages/unread`, options)
      .pipe(
        map(response => response.data || response as any),
        timeout(10000),
        catchError(this.handleError)
      );
  }

  getUnreadCount(): Observable<{count: number}> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<{count: number}>>(`${this.baseUrl}/messages/count-unread`, options)
      .pipe(
        map(response => response.data || response as any),
        timeout(10000),
        catchError(this.handleError)
      );
  }

  markAsRead(id: number): Observable<ContactMessage> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.patch<ApiResponse<ContactMessage>>(`${this.baseUrl}/messages/${id}/read`, {}, options)
      .pipe(
        map(response => response.data || response as any),
        timeout(10000),
        catchError(this.handleError)
      );
  }

  markAllAsRead(): Observable<{success: boolean}> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.patch<{success: boolean}>(`${this.baseUrl}/messages/mark-all-read`, {}, options)
      .pipe(
        timeout(10000),
        catchError(this.handleError)
      );
  }

  replyToMessage(messageId: number, replyData: {to: string, subject: string, message: string}): Observable<{success: boolean}> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<{success: boolean}>(`${this.baseUrl}/messages/${messageId}/reply`, replyData, options)
      .pipe(
        timeout(10000),
        catchError(this.handleError)
      );
  }

  deleteMessage(id: number): Observable<any> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<any>(`${this.baseUrl}/messages/${id}`, options)
      .pipe(
        map(response => response.data || response),
        timeout(10000),
        catchError(this.handleError)
      );
  }

  submitContactForm(contactData: ContactMessage): Observable<any> {
    // Contact form submission doesn't require authentication (public endpoint)
    return this.http.post<any>(this.baseUrl, contactData)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else {
      switch (error.status) {
        case 401:
          console.warn('Unauthorized access in contact service - logging out');
          this.authService.logout();
          errorMessage = 'Unauthorized: Full authentication is required to access this resource';
          break;
        case 400: 
          errorMessage = error.error?.message || 'Invalid request'; 
          break;
        case 403:
          errorMessage = 'Access denied. You do not have permission to perform this action.';
          break;
        case 404:
          errorMessage = 'Resource not found';
          break;
        case 500: 
          errorMessage = 'Internal server error'; 
          break;
        default: 
          errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    
    console.error('Contact service error:', {
      status: error.status,
      message: errorMessage,
      url: error.url,
      error: error.error
    });
    
    return throwError(() => new Error(errorMessage));
  }
}