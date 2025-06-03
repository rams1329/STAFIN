import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { LoginRequest, RegisterRequest } from '../../../models/user.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  registerForm: FormGroup;
  isLoginMode = true;
  
  showPassword = false;
  showRegisterPassword = false;
  showConfirmPassword = false;
  
  isLoginLoading = false;
  isRegisterLoading = false;
  
  returnUrl = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });

    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Redirect if already authenticated
    if (this.authService.isAuthenticated()) {
      if (this.authService.isAdmin()) {
        this.router.navigate(['/admin/dashboard']);
      } else {
        this.router.navigate(['/user/dashboard']);
      }
      return;
    }

    // Get return url from route parameters
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
  }

  switchToLogin(): void {
    this.isLoginMode = true;
  }

  switchToRegister(): void {
    this.isLoginMode = false;
  }

  onLogin(): void {
    if (this.loginForm.valid && !this.isLoginLoading) {
      this.isLoginLoading = true;

      const credentials: LoginRequest = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.isLoginLoading = false;
          this.toastService.loginSuccess(response.user?.name);
          // Note: Redirection is handled automatically by AuthService
        },
        error: (error) => {
          this.isLoginLoading = false;
          this.toastService.loginFailed(error.message || 'Invalid credentials. Please try again.');
        }
      });
    } else if (this.loginForm.invalid) {
      this.toastService.validationError('Please fill all required fields correctly.');
    }
  }

  onRegister(): void {
    if (this.registerForm.valid && !this.passwordMismatch && !this.isRegisterLoading) {
      this.isRegisterLoading = true;

      const userData: RegisterRequest = {
        name: this.registerForm.value.name,
        email: this.registerForm.value.email,
        password: this.registerForm.value.password
      };

      this.authService.register(userData).subscribe({
        next: (response) => {
          this.isRegisterLoading = false;
          this.toastService.success('Registration successful! You can now sign in.', 'Account Created');
          this.switchToLogin();
          this.loginForm.patchValue({ email: userData.email });
        },
        error: (error) => {
          this.isRegisterLoading = false;
          this.toastService.error(error.message || 'Registration failed. Please try again.', 'Registration Failed');
        }
      });
    } else if (this.registerForm.invalid || this.passwordMismatch) {
      this.toastService.validationError('Please fix the form errors and try again.');
    }
  }

  get passwordMismatch(): boolean {
    const password = this.registerForm.get('password')?.value;
    const confirmPassword = this.registerForm.get('confirmPassword')?.value;
    return password && confirmPassword && password !== confirmPassword;
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleRegisterPassword(): void {
    this.showRegisterPassword = !this.showRegisterPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  isLoginFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isRegisterFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
} 