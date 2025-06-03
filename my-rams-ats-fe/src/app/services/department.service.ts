import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface Department {
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
export class DepartmentService {
  private baseUrl = `${environment.apiUrl}/api/departments`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found for department service');
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
  getDepartmentsWithFilters(params: any): Observable<any> {
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

    console.log('🚀 Making departments API call to:', this.baseUrl);
    console.log('📋 With params:', httpParams.toString());

    return this.http.get<ApiResponse<any>>(this.baseUrl, { 
      ...options, 
      params: httpParams 
    }).pipe(
      map(response => {
        console.log('🔍 Departments API response:', response);
        // Return the data from ApiResponse
        return response.data || response;
      }),
      catchError(this.handleError)
    );
  }

  getAllDepartments(): Observable<Department[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<Department[]>>(this.baseUrl, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  getActiveDepartments(): Observable<Department[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<Department[]>>(`${this.baseUrl}/active`, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  getDepartmentById(id: number): Observable<Department> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<Department>>(`${this.baseUrl}/${id}`, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  createDepartment(department: Department): Observable<Department> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<Department>>(this.baseUrl, department, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  updateDepartment(id: number, department: Department): Observable<Department> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<Department>>(`${this.baseUrl}/${id}`, department, options)
      .pipe(
        map(response => response.data || response as any),
        catchError(this.handleError)
      );
  }

  deleteDepartment(id: number): Observable<void> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<void>(`${this.baseUrl}/${id}`, options)
      .pipe(
        catchError(this.handleError)
      );
  }

  toggleDepartmentActive(id: number): Observable<Department> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<Department>>(`${this.baseUrl}/${id}/toggle-active`, {}, options)
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
          console.warn('Unauthorized access in department service - logging out');
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
          errorMessage = 'Department not found';
          break;
        case 500: 
          errorMessage = 'Internal server error'; 
          break;
        default: 
          errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    
    console.error('Department service error:', {
      status: error.status,
      message: errorMessage,
      url: error.url,
      error: error.error
    });
    
    return throwError(() => new Error(errorMessage));
  }
} 