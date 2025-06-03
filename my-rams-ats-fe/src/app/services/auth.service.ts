import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { User, LoginRequest, AuthResponse, RegisterRequest, ForgotPasswordRequest, OtpVerificationRequest } from '../models/user.model';

export interface UpdateProfileRequest {
  name: string;
  currentPassword?: string;
  newPassword?: string;
}

export interface PasswordResetRequest {
  oldPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/api/auth`;
  private userBaseUrl = `${environment.apiUrl}/api/users`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private tokenKey = environment.tokenKey;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    const token = this.getToken();
    const savedUser = localStorage.getItem('user');
    
    // Initialize user from localStorage first
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Error parsing saved user data:', error);
        localStorage.removeItem('user');
      }
    }
    
    // Then validate token
    if (token && !this.isTokenExpired(token)) {
      // Only fetch fresh profile if we don't have user data or on app startup
      if (!savedUser) {
        this.loadUserProfile();
      }
    } else {
      // Clear invalid token and user data
      this.clearAuthData();
    }
  }

  // Unified login for both admin and users
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<any>(`${this.baseUrl}/login`, credentials)
      .pipe(
        map(response => {
          const authData = response.data || response;
          return {
            token: authData.token,
            type: authData.type || 'Bearer',
            user: {
              id: authData.id,
              name: authData.name,
              email: authData.email,
              firstLogin: authData.firstLogin,
              active: true,
              roles: [{ id: authData.role === 'ROLE_ADMIN' ? 2 : 1, name: authData.role, description: authData.role }],
              isAdmin: authData.isAdmin || authData.role === 'ROLE_ADMIN'
            }
          } as AuthResponse;
        }),
        tap(authResponse => {
          const token = authResponse.token;
          this.setToken(token);
          localStorage.setItem('user', JSON.stringify(authResponse.user));
          this.currentUserSubject.next(authResponse.user);
          
          // Role-based redirection
          this.redirectAfterLogin(authResponse.user);
        }),
        catchError(error => this.handleError(error))
      );
  }

  // User registration
  register(userData: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/register`, userData)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Forgot password
  forgotPassword(request: ForgotPasswordRequest): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/forgot-password`, request)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // OTP verification and password reset
  verifyOtp(request: OtpVerificationRequest): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/verify-otp`, request)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Update user profile
  updateProfile(profileData: UpdateProfileRequest): Observable<User> {
    return this.http.put<any>(`${this.userBaseUrl}/update-profile`, profileData)
      .pipe(
        map(response => {
          const userData = response.data || response;
          const user: User = {
            id: userData.id,
            name: userData.name,
            email: userData.email,
            firstLogin: false,
            active: true,
            roles: userData.roles?.map((roleName: string) => ({ 
              id: roleName === 'ROLE_ADMIN' ? 2 : 1, 
              name: roleName, 
              description: roleName === 'ROLE_ADMIN' ? 'Administrator' : 'User' 
            })) || [{ id: 1, name: 'ROLE_USER', description: 'User' }],
            isAdmin: userData.roles?.includes('ROLE_ADMIN') || false
          };
          
          // Update local storage and current user
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
          
          return user;
        }),
        catchError(error => this.handleError(error))
      );
  }

  // Reset password
  resetPassword(passwordData: PasswordResetRequest): Observable<any> {
    return this.http.post<any>(`${this.userBaseUrl}/reset-password`, passwordData)
      .pipe(
        map(response => response.data || response),
        catchError(error => this.handleError(error))
      );
  }

  // Get user profile from backend
  getUserProfile(): Observable<User> {
    return this.http.get<any>(`${this.userBaseUrl}/profile`)
      .pipe(
        map(response => {
          const userData = response.data || response;
          const user: User = {
            id: userData.id,
            name: userData.name,
            email: userData.email,
            firstLogin: userData.firstLogin || false,
            active: userData.active || true,
            roles: userData.roles?.map((roleName: string) => ({ 
              id: roleName === 'ROLE_ADMIN' ? 2 : 1, 
              name: roleName, 
              description: roleName === 'ROLE_ADMIN' ? 'Administrator' : 'User' 
            })) || [{ id: 1, name: 'ROLE_USER', description: 'User' }],
            isAdmin: userData.roles?.includes('ROLE_ADMIN') || false
          };
          
          // Update local storage and current user
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
          
          return user;
        }),
        catchError(error => this.handleError(error))
      );
  }

  // Role-based redirection after login
  private redirectAfterLogin(user: User): void {
    // Check for first login and redirect to password reset
    if (user.firstLogin) {
      this.router.navigate(['/forgot-password'], { 
        queryParams: { email: user.email, firstLogin: true } 
      });
      return;
    }

    if (user.isAdmin || this.hasRole('ROLE_ADMIN')) {
      this.router.navigate(['/admin/dashboard']);
    } else {
      this.router.navigate(['/user/dashboard']);
    }
  }

  logout(): Observable<void> {
    this.clearAuthData();
    return of(void 0);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.tokenKey);
    if (token && this.isTokenExpired(token)) {
      console.warn('Token found but expired, clearing authentication data');
      this.clearAuthData();
      return null;
    }
    return token;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const isValid = token !== null && !this.isTokenExpired(token);
    console.debug('Authentication check - Token present:', !!token, 'Is valid:', isValid);
    return isValid;
  }

  hasRole(roleName: string): boolean {
    const user = this.getCurrentUser();
    // Standardize role checking to use ROLE_ prefix
    if (roleName === 'ADMIN') {
      roleName = 'ROLE_ADMIN';
    } else if (roleName === 'USER') {
      roleName = 'ROLE_USER';
    }
    
    if (roleName === 'ROLE_ADMIN') {
      return user?.isAdmin === true || user?.roles?.some(role => role.name === 'ROLE_ADMIN') || false;
    }
    return user?.roles?.some(role => role.name === roleName) || false;
  }

  hasAnyRole(roleNames: string[]): boolean {
    return roleNames.some(role => this.hasRole(role));
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isUser(): boolean {
    return this.isAuthenticated() && !this.isAdmin();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch (error) {
      return true;
    }
  }

  private loadUserProfile(): void {
    this.getUserProfile().subscribe({
      next: (user) => {
        console.log('✅ User profile loaded successfully:', user);
      },
      error: (error) => {
        console.error('❌ Failed to load user profile:', error);
        this.clearAuthData();
      }
    });
  }

  getProfile(): Observable<User> {
    const user = this.getCurrentUser();
    if (user) {
      return new Observable<User>(observer => {
        observer.next(user);
        observer.complete();
      });
    }
    this.clearAuthData();
    return throwError(() => new Error('No user found'));
  }

  private handleError(error: HttpErrorResponse | any): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.name === 'TimeoutError') {
      errorMessage = 'Connection timeout. Please check if the server is running.';
    } else if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else {
      switch (error.status) {
        case 401: errorMessage = error.error?.message || 'Invalid credentials'; break;
        case 403: errorMessage = 'Access denied'; break;
        case 404: errorMessage = 'Service not found. Please check if the server is running.'; break;
        case 500: errorMessage = 'Internal server error'; break;
        default: errorMessage = error.error?.message || `Error Code: ${error.status}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
