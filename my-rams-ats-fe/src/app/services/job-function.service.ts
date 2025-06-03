import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface JobFunction {
  id?: number;
  name: string;
  description?: string;
  isActive: boolean;
  createdAt?: Date;
  modifiedAt?: Date;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class JobFunctionService {
  private baseUrl = `${environment.apiUrl}/api/job-functions`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found for job function service');
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
  getJobFunctionsWithFilters(params: any): Observable<any> {
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

    console.log('🚀 Making job functions API call to:', this.baseUrl);
    console.log('📋 With params:', httpParams.toString());

    return this.http.get<ApiResponse<any>>(this.baseUrl, { 
      ...options, 
      params: httpParams 
    }).pipe(
      map(response => {
        console.log('🔍 Job Functions API response:', response);
        // Return the data from ApiResponse
        return response.data || response;
      }),
      catchError(this.handleError)
    );
  }

  getAllJobFunctions(): Observable<JobFunction[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<JobFunction[]>>(this.baseUrl, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  getActiveJobFunctions(): Observable<JobFunction[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<JobFunction[]>>(`${this.baseUrl}/active`, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  getJobFunctionById(id: number): Observable<JobFunction> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<JobFunction>>(`${this.baseUrl}/${id}`, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  createJobFunction(jobFunction: JobFunction): Observable<JobFunction> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<JobFunction>>(this.baseUrl, jobFunction, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  updateJobFunction(id: number, jobFunction: JobFunction): Observable<JobFunction> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<JobFunction>>(`${this.baseUrl}/${id}`, jobFunction, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  deleteJobFunction(id: number): Observable<void> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<void>(`${this.baseUrl}/${id}`, options)
      .pipe(
        catchError(this.handleError)
      );
  }

  toggleJobFunctionActive(id: number): Observable<JobFunction> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<JobFunction>>(`${this.baseUrl}/${id}/toggle-active`, {}, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
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
          console.warn('Unauthorized access in job function service - logging out');
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
          errorMessage = 'Job Function not found';
          break;
        case 500: 
          errorMessage = 'Internal server error'; 
          break;
        default: 
          errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    
    console.error('Job Function service error:', {
      status: error.status,
      message: errorMessage,
      url: error.url,
      error: error.error
    });
    
    return throwError(() => new Error(errorMessage));
  }
} 