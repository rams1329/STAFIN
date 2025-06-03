import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { JobPosting } from '../models/job.model';

export interface JobPostingRequest {
  requirementTitle: string;
  jobTitle: string;
  jobDesignation: string;
  jobDescription: string;
  jobFunctionId: number;
  jobTypeId: number;
  locationId: number;
  departmentId?: number;
  experienceLevelId?: number;
  salaryMin?: number;
  salaryMax?: number;
  isActive: boolean;
}

export interface DashboardStats {
  totalJobs: number;
  activeUsers: number;
  unreadMessages: number;
  applications: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface JobSearchParams {
  title?: string;
  requirementTitle?: string;
  jobFunctionId?: number;
  jobTypeId?: number;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private baseUrl = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  // Public job methods (no authentication required)
  getPublicJobs(): Observable<JobPosting[]> {
    return this.http.get<any>(`${this.baseUrl}/public/jobs`)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Enhanced public job method with filters, search, sorting, and pagination
  getPublicJobsWithFilters(params: any): Observable<any> {
    let httpParams = new HttpParams();
    
    // Add all parameters to HttpParams
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    console.log('🚀 Making public jobs API call to:', `${this.baseUrl}/jobs`);
    console.log('📋 With params:', httpParams.toString());

    return this.http.get<any>(`${this.baseUrl}/jobs`, { params: httpParams })
      .pipe(
        map(response => {
          console.log('🔍 Public jobs API response:', response);
          // Return the full paginated response for pagination info
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('❌ Public jobs API Error:', {
            status: error.status,
            statusText: error.statusText,
            message: error.message,
            error: error.error,
            url: error.url
          });
          return this.handleError(error);
        })
      );
  }

  getPublicJobById(id: number): Observable<JobPosting> {
    return this.http.get<any>(`${this.baseUrl}/public/jobs/${id}`)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Authenticated job methods
  getAllJobs(params?: JobSearchParams): Observable<any> {
    let httpParams = new HttpParams();
    
    if (params) {
      if (params.title) httpParams = httpParams.set('title', params.title);
      if (params.requirementTitle) httpParams = httpParams.set('requirementTitle', params.requirementTitle);
      if (params.jobFunctionId) httpParams = httpParams.set('jobFunctionId', params.jobFunctionId.toString());
      if (params.jobTypeId) httpParams = httpParams.set('jobTypeId', params.jobTypeId.toString());
      if (params.page) httpParams = httpParams.set('page', params.page.toString());
      if (params.size) httpParams = httpParams.set('size', params.size.toString());
    }

    console.log('🚀 Making API call to:', `${this.baseUrl}/admin/jobs`);
    console.log('📋 With params:', httpParams.toString());

    // Use admin endpoint for better admin access
    return this.http.get<any>(`${this.baseUrl}/admin/jobs`, { params: httpParams })
      .pipe(
        map(response => {
          console.log('🔍 Raw API response:', response);
          // Handle paginated response from Spring Boot
          if (response && response.content) {
            console.log('📄 Paginated response with', response.content.length, 'items');
            return response.content; // Return just the content array
          } else if (Array.isArray(response)) {
            console.log('📋 Direct array response with', response.length, 'items');
            return response;
          } else if (response && response.data) {
            console.log('📦 Wrapped response with data property');
            return response.data;
          } else {
            console.warn('⚠️ Unexpected response format:', response);
            return [];
          }
        }),
        catchError(error => {
          console.error('❌ API Error details:', {
            status: error.status,
            statusText: error.statusText,
            message: error.message,
            error: error.error,
            url: error.url
          });
          return this.handleError(error);
        })
      );
  }

  // Enhanced method with comprehensive filters
  getAllJobsWithFilters(params: any): Observable<any> {
    let httpParams = new HttpParams();
    
    // Add all parameters to HttpParams
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    console.log('🚀 Making enhanced API call to:', `${this.baseUrl}/admin/jobs`);
    console.log('📋 With enhanced params:', httpParams.toString());

    return this.http.get<any>(`${this.baseUrl}/admin/jobs`, { params: httpParams })
      .pipe(
        map(response => {
          console.log('🔍 Enhanced API response:', response);
          // Return the full paginated response for pagination info
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('❌ Enhanced API Error details:', {
            status: error.status,
            statusText: error.statusText,
            message: error.message,
            error: error.error,
            url: error.url
          });
          return this.handleError(error);
        })
      );
  }

  getJobById(id: number): Observable<JobPosting> {
    return this.http.get<any>(`${this.baseUrl}/jobs/${id}`)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Helper method to get fresh job data (bypasses any caching)
  getFreshJobById(id: number): Observable<JobPosting> {
    const timestamp = new Date().getTime();
    return this.http.get<any>(`${this.baseUrl}/jobs/${id}?_t=${timestamp}`)
      .pipe(
        map(response => {
          console.log('✅ Fresh job data loaded:', response);
          return response.data || response;
        }),
        catchError(error => this.handleError(error))
      );
  }

  // Helper method to get fresh public job data
  getFreshPublicJobById(id: number): Observable<JobPosting> {
    const timestamp = new Date().getTime();
    return this.http.get<any>(`${this.baseUrl}/public/jobs/${id}?_t=${timestamp}`)
      .pipe(
        map(response => {
          console.log('✅ Fresh public job data loaded:', response);
          return response.data || response;
        }),
        catchError(error => this.handleError(error))
      );
  }

  // Method to refresh job data across all components
  refreshJobData(jobId: number): Observable<JobPosting> {
    console.log(`🔄 Refreshing job data for ID: ${jobId}`);
    return this.getFreshJobById(jobId);
  }

  // Admin job methods
  createJob(jobData: any): Observable<JobPosting> {
    return this.http.post<any>(`${this.baseUrl}/admin/jobs`, jobData)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  updateJob(id: number, jobData: any): Observable<JobPosting> {
    return this.http.put<any>(`${this.baseUrl}/admin/jobs/${id}`, jobData)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  deleteJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/admin/jobs/${id}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  toggleJobStatus(id: number): Observable<JobPosting> {
    return this.http.patch<any>(`${this.baseUrl}/jobs/${id}/toggle-active`, {})
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Dashboard APIs
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<ApiResponse<DashboardStats>>(`${this.baseUrl}/dashboard/stats`)
      .pipe(
        map(response => response.data || response as any),
        catchError(error => this.handleError(error))
      );
  }

  getRecentJobs(limit: number = 2): Observable<JobPosting[]> {
    return this.http.get<ApiResponse<JobPosting[]>>(`${this.baseUrl}/dashboard/recent-jobs?limit=${limit}`)
      .pipe(
        map(response => response.data || response as any),
        catchError(error => this.handleError(error))
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else {
      switch (error.status) {
        case 401: errorMessage = 'Unauthorized access'; break;
        case 403: errorMessage = 'Access denied'; break;
        case 404: errorMessage = 'Job not found'; break;
        case 500: errorMessage = 'Internal server error'; break;
        default: errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
} 