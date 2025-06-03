import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, timeout, catchError, throwError, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, JobFunction, JobType, Location, ExperienceLevel, Department } from '../models/api-response.interface';

@Injectable({
  providedIn: 'root'
})
export class DropdownService {
  private readonly baseUrl = `${environment.apiUrl}/api`;
  private readonly adminUrl = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  // ========================================
  // ALL ENDPOINTS ARE NOW AUTHENTICATED (ADMIN ONLY)
  // ========================================

  getJobFunctions(): Observable<JobFunction[]> {
    console.log('🔑 Fetching job functions (AUTH REQUIRED)');
    return this.http.get<ApiResponse<JobFunction[]>>(`${this.adminUrl}/job-functions`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Job Functions API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getJobTypes(): Observable<JobType[]> {
    console.log('🔑 Fetching job types (AUTH REQUIRED)');
    return this.http.get<ApiResponse<JobType[]>>(`${this.adminUrl}/job-types`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Job Types API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getLocations(): Observable<Location[]> {
    console.log('🔑 Fetching locations (AUTH REQUIRED)');
    return this.http.get<ApiResponse<Location[]>>(`${this.adminUrl}/locations`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Locations API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getExperienceLevels(): Observable<ExperienceLevel[]> {
    console.log('🔑 Fetching experience levels (AUTH REQUIRED)');
    return this.http.get<ApiResponse<ExperienceLevel[]>>(`${this.adminUrl}/experience-levels`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Experience Levels API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getDepartments(): Observable<Department[]> {
    console.log('🔑 Fetching departments (AUTH REQUIRED)');
    return this.http.get<ApiResponse<Department[]>>(`${this.adminUrl}/departments`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Departments API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  // ========================================
  // ADMIN CRUD OPERATIONS
  // ========================================

  // Job Functions
  createJobFunction(jobFunction: Partial<JobFunction>): Observable<JobFunction> {
    return this.http.post<ApiResponse<JobFunction>>(`${this.adminUrl}/job-functions`, jobFunction, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateJobFunction(id: number, jobFunction: Partial<JobFunction>): Observable<JobFunction> {
    return this.http.put<ApiResponse<JobFunction>>(`${this.adminUrl}/job-functions/${id}`, jobFunction, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteJobFunction(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.adminUrl}/job-functions/${id}`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  toggleJobFunctionActive(id: number): Observable<JobFunction> {
    return this.http.put<ApiResponse<JobFunction>>(`${this.adminUrl}/job-functions/${id}/toggle-active`, {}, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  // Job Types
  createJobType(jobType: Partial<JobType>): Observable<JobType> {
    return this.http.post<ApiResponse<JobType>>(`${this.adminUrl}/job-types`, jobType, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateJobType(id: number, jobType: Partial<JobType>): Observable<JobType> {
    return this.http.put<ApiResponse<JobType>>(`${this.adminUrl}/job-types/${id}`, jobType, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteJobType(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.adminUrl}/job-types/${id}`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  toggleJobTypeActive(id: number): Observable<JobType> {
    return this.http.put<ApiResponse<JobType>>(`${this.adminUrl}/job-types/${id}/toggle-active`, {}, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  // Locations
  createLocation(location: Partial<Location>): Observable<Location> {
    return this.http.post<ApiResponse<Location>>(`${this.adminUrl}/locations`, location, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateLocation(id: number, location: Partial<Location>): Observable<Location> {
    return this.http.put<ApiResponse<Location>>(`${this.adminUrl}/locations/${id}`, location, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteLocation(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.adminUrl}/locations/${id}`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  toggleLocationActive(id: number): Observable<Location> {
    return this.http.put<ApiResponse<Location>>(`${this.adminUrl}/locations/${id}/toggle-active`, {}, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  // Experience Levels
  createExperienceLevel(experienceLevel: Partial<ExperienceLevel>): Observable<ExperienceLevel> {
    return this.http.post<ApiResponse<ExperienceLevel>>(`${this.adminUrl}/experience-levels`, experienceLevel, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateExperienceLevel(id: number, experienceLevel: Partial<ExperienceLevel>): Observable<ExperienceLevel> {
    return this.http.put<ApiResponse<ExperienceLevel>>(`${this.adminUrl}/experience-levels/${id}`, experienceLevel, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteExperienceLevel(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.adminUrl}/experience-levels/${id}`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  toggleExperienceLevelActive(id: number): Observable<ExperienceLevel> {
    return this.http.put<ApiResponse<ExperienceLevel>>(`${this.adminUrl}/experience-levels/${id}/toggle-active`, {}, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  // Departments
  createDepartment(department: Partial<Department>): Observable<Department> {
    return this.http.post<ApiResponse<Department>>(`${this.adminUrl}/departments`, department, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateDepartment(id: number, department: Partial<Department>): Observable<Department> {
    return this.http.put<ApiResponse<Department>>(`${this.adminUrl}/departments/${id}`, department, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteDepartment(id: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.adminUrl}/departments/${id}`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  toggleDepartmentActive(id: number): Observable<Department> {
    return this.http.put<ApiResponse<Department>>(`${this.adminUrl}/departments/${id}/toggle-active`, {}, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  // ========================================
  // UTILITY METHODS
  // ========================================

  // HTTP Options with JWT token
  private getHttpOptions() {
    const token = localStorage.getItem(environment.tokenKey);
    
    if (!token) {
      console.error('No authentication token found! User must login first.');
    }
    
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }

  // Enhanced error handling
  private handleError = (error: HttpErrorResponse | any): Observable<never> => {
    let errorMessage = 'An error occurred';
    
    if (error.name === 'TimeoutError') {
      errorMessage = 'Request timeout. Please check if the server is running.';
    } else if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      // Network error or CORS issue
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else {
      // Server-side error
      switch (error.status) {
        case 401:
          errorMessage = 'Unauthorized access - Please login as admin user';
          console.error('401 Error: Authentication required. Clearing stored tokens...');
          // Clear token and redirect to login
          localStorage.removeItem(environment.tokenKey);
          localStorage.removeItem('user');
          break;
        case 403:
          errorMessage = 'Access forbidden - Admin role required';
          break;
        case 404:
          errorMessage = 'Service not found. Please check if the server is running.';
          break;
        case 500:
          errorMessage = 'Internal server error';
          break;
        default:
          errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }

  // ========================================
  // ACTIVE-ONLY METHODS FOR FORM DROPDOWNS
  // ========================================

  getActiveJobFunctions(): Observable<JobFunction[]> {
    console.log('🔑 Fetching ACTIVE job functions for dropdowns (AUTH REQUIRED)');
    return this.http.get<ApiResponse<JobFunction[]>>(`${this.adminUrl}/job-functions/active`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Active Job Functions API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getActiveJobTypes(): Observable<JobType[]> {
    console.log('🔑 Fetching ACTIVE job types for dropdowns (AUTH REQUIRED)');
    return this.http.get<ApiResponse<JobType[]>>(`${this.adminUrl}/job-types/active`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Active Job Types API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getActiveLocations(): Observable<Location[]> {
    console.log('🔑 Fetching ACTIVE locations for dropdowns (AUTH REQUIRED)');
    return this.http.get<ApiResponse<Location[]>>(`${this.adminUrl}/locations/active`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Active Locations API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getActiveExperienceLevels(): Observable<ExperienceLevel[]> {
    console.log('🔑 Fetching ACTIVE experience levels for dropdowns (AUTH REQUIRED)');
    return this.http.get<ApiResponse<ExperienceLevel[]>>(`${this.adminUrl}/experience-levels/active`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Active Experience Levels API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }

  getActiveDepartments(): Observable<Department[]> {
    console.log('🔑 Fetching ACTIVE departments for dropdowns (AUTH REQUIRED)');
    return this.http.get<ApiResponse<Department[]>>(`${this.adminUrl}/departments/active`, this.getHttpOptions())
      .pipe(
        timeout(10000),
        map(response => {
          console.log('✅ Active Departments API Response:', response);
          return response.data || [];
        }),
        catchError(this.handleError)
      );
  }
} 