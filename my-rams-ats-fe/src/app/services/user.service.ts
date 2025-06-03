import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, map, catchError, timeout } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.interface';
import { AuthService } from './auth.service';

export interface User {
  id: number;
  name: string;
  email: string;
  firstLogin: boolean;
  active: boolean;
  roles: Role[];
  createdDate?: Date;
  lastLoginDate?: Date;
}

export interface Role {
  id: number;
  name: string;
  description: string;
}

export interface AdminCreateUserRequest {
  name: string;
  email: string;
  roleIds: number[];
}

export interface UpdateUserRequest {
  name: string;
  email: string;
  roleIds: number[];
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = `${environment.apiUrl}/api`;
  private authUrl = `${environment.apiUrl}/api/auth`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // ========================================
  // HELPER METHODS
  // ========================================

  private getHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found for user service');
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

  // ========================================
  // USER MANAGEMENT APIs
  // ========================================

  // Get all users (Admin only)
  getUsers(): Observable<User[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<User[]>(`${this.baseUrl}/users`, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Get user by ID
  getUserById(id: number): Observable<User> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<User>(`${this.baseUrl}/users/${id}`, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Create user by admin
  createUserByAdmin(userData: AdminCreateUserRequest): Observable<any> {
    console.log('🔑 Creating user by admin:', userData);
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<any>>(`${this.authUrl}/admin/create`, userData, options)
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Admin Create User Response:', response);
          return response;
        }),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Update user
  updateUser(id: number, userData: UpdateUserRequest): Observable<User> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, userData, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Delete user
  deleteUser(id: number): Observable<any> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<any>(`${this.baseUrl}/users/${id}`, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Toggle user status
  toggleUserStatus(id: number): Observable<User> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.patch<User>(`${this.baseUrl}/users/${id}/toggle-active`, {}, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Reset user password
  resetPassword(id: number): Observable<any> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<any>(`${this.baseUrl}/users/${id}/reset-password`, {}, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // ========================================
  // ROLE MANAGEMENT APIs
  // ========================================

  // Get all roles
  getAllRoles(): Observable<Role[]> {
    console.log('🔑 Fetching all roles');
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<Role[]>(`${this.baseUrl}/roles`, options)
      .pipe(
        timeout(10000),
        map(roles => {
          console.log('✅ Roles API Response:', roles);
          return roles;
        }),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  // Get role by ID
  getRoleById(id: number): Observable<Role> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<Role>(`${this.baseUrl}/roles/${id}`, options)
      .pipe(
        timeout(10000),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access in user service - logging out');
            this.authService.logout();
          }
          return this.handleError(error);
        })
      );
  }

  private handleError = (error: HttpErrorResponse | any): Observable<never> => {
    console.error('❌ User Service Error:', error);
    
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client Error: ${error.error.message}`;
    } else if (error.status) {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Bad request - please check your input';
          break;
        case 401:
          errorMessage = 'Unauthorized - please log in again';
          break;
        case 403:
          errorMessage = 'Forbidden - you do not have permission for this action';
          break;
        case 409:
          errorMessage = error.error?.message || 'Conflict - email already in use';
          break;
        case 500:
          errorMessage = 'Server error - please try again later';
          break;
        default:
          errorMessage = error.error?.message || `Server returned error code ${error.status}`;
      }
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => new Error(errorMessage));
  };
} 