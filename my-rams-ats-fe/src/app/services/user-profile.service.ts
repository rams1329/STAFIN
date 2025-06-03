import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { UserBasicInfo, UserEducation, UserEmployment, ApiResponse } from '../models/user-profile.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  private baseUrl = `${environment.apiUrl}/api/user/profile`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found');
      // Redirect to login if no token
      this.authService.logout();
      return throwError(() => new Error('Authentication required'));
    }
    
    console.debug('Making profile API request with token:', token.substring(0, 20) + '...');
    
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }

  private getMultipartHttpOptions() {
    const token = this.authService.getToken();
    if (!token) {
      console.warn('No authentication token found');
      // Redirect to login if no token
      this.authService.logout();
      return throwError(() => new Error('Authentication required'));
    }
    
    console.debug('Making profile API multipart request with token:', token.substring(0, 20) + '...');
    
    return {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`
      })
    };
  }

  // Basic Information
  getUserBasicInfo(): Observable<UserBasicInfo> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<UserBasicInfo>>(`${this.baseUrl}/basic-info`, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  saveUserBasicInfo(basicInfo: UserBasicInfo): Observable<UserBasicInfo> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<UserBasicInfo>>(`${this.baseUrl}/basic-info`, basicInfo, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  uploadResume(file: File): Observable<string> {
    const options = this.getMultipartHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/resume`, formData, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  // Education Information
  getUserEducation(): Observable<UserEducation[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<UserEducation[]>>(`${this.baseUrl}/education`, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  saveUserEducation(education: UserEducation): Observable<UserEducation> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<UserEducation>>(`${this.baseUrl}/education`, education, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  updateUserEducation(educationId: number, education: UserEducation): Observable<UserEducation> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<UserEducation>>(`${this.baseUrl}/education/${educationId}`, education, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  deleteUserEducation(educationId: number): Observable<void> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/education/${educationId}`, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  // Employment Information
  getUserEmployment(): Observable<UserEmployment[]> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.get<ApiResponse<UserEmployment[]>>(`${this.baseUrl}/employment`, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  saveUserEmployment(employment: UserEmployment): Observable<UserEmployment> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.post<ApiResponse<UserEmployment>>(`${this.baseUrl}/employment`, employment, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  updateUserEmployment(employmentId: number, employment: UserEmployment): Observable<UserEmployment> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.put<ApiResponse<UserEmployment>>(`${this.baseUrl}/employment/${employmentId}`, employment, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }

  deleteUserEmployment(employmentId: number): Observable<void> {
    const options = this.getHttpOptions();
    if (options instanceof Observable) {
      return options; // Return the error observable
    }
    
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/employment/${employmentId}`, options)
      .pipe(
        map(response => response.data),
        catchError(error => {
          if (error.status === 401) {
            console.warn('Unauthorized access - logging out');
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
  }
} 