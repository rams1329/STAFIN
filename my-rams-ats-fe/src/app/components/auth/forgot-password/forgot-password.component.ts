import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { ForgotPasswordRequest, OtpVerificationRequest } from '../../../models/user.model';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm: FormGroup;
  otpForm: FormGroup;
  isOtpMode = false;
  isForgotLoading = false;
  isOtpLoading = false;
  errorMessage = '';
  successMessage = '';
  showNewPassword = false;
  showConfirmPassword = false;
  resendCooldown = 0;
  resendTimer: any;
  isFirstLogin = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.otpForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Check for first login redirection
    this.route.queryParams.subscribe(params => {
      if (params['firstLogin'] === 'true' && params['email']) {
        this.isFirstLogin = true;
        this.forgotPasswordForm.patchValue({ email: params['email'] });
        // Automatically trigger the forgot password process for first-time users
        this.onForgotPassword();
      }
    });

    // Redirect if already authenticated (but not for first login)
    if (this.authService.isAuthenticated() && !this.isFirstLogin) {
      if (this.authService.isAdmin()) {
        this.router.navigate(['/admin/dashboard']);
      } else {
        this.router.navigate(['/user/dashboard']);
      }
    }
  }

  getHeaderTitle(): string {
    if (this.isFirstLogin) {
      return this.isOtpMode ? 'Complete Account Setup' : 'Account Setup Required';
    }
    return this.isOtpMode ? 'Verify OTP' : 'Reset Password';
  }

  getHeaderDescription(): string {
    if (this.isFirstLogin) {
      return this.isOtpMode ? 'Enter the setup code and create your password' : 'Your account is ready! Please set up your password';
    }
    return this.isOtpMode ? 'Enter the OTP sent to your email and set a new password' : 'Enter your email to receive a password reset code';
  }

  onForgotPassword(): void {
    if (this.forgotPasswordForm.valid && !this.isForgotLoading) {
      this.isForgotLoading = true;
      this.clearMessages();

      const request: ForgotPasswordRequest = {
        email: this.forgotPasswordForm.value.email
      };

      this.authService.forgotPassword(request).subscribe({
        next: (response) => {
          this.isForgotLoading = false;
          if (this.isFirstLogin) {
            this.successMessage = 'Setup code sent to your email. Please check your inbox.';
          } else {
            this.successMessage = 'Reset code sent to your email. Please check your inbox.';
          }
          this.switchToOtpMode(request.email);
        },
        error: (error) => {
          this.isForgotLoading = false;
          this.errorMessage = error.message || 'Failed to send reset code. Please try again.';
          this.autoHideError();
        }
      });
    }
  }

  onVerifyOtp(): void {
    if (this.otpForm.valid && !this.passwordMismatch && !this.isOtpLoading) {
      this.isOtpLoading = true;
      this.clearMessages();

      const request: OtpVerificationRequest = {
        email: this.otpForm.get('email')?.value,
        otp: this.otpForm.value.otp,
        newPassword: this.otpForm.value.newPassword
      };

      this.authService.verifyOtp(request).subscribe({
        next: (response) => {
          this.isOtpLoading = false;
          if (this.isFirstLogin) {
            this.successMessage = 'Account setup complete! You can now login with your new password.';
          } else {
            this.successMessage = 'Password reset successful! You can now login with your new password.';
          }
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.isOtpLoading = false;
          this.errorMessage = error.message || 'Verification failed. Please try again.';
          this.autoHideError();
        }
      });
    }
  }

  resendOtp(): void {
    if (this.resendCooldown === 0) {
      const email = this.otpForm.get('email')?.value;
      if (email) {
        this.onForgotPassword();
        this.startResendCooldown();
      }
    }
  }

  private switchToOtpMode(email: string): void {
    this.isOtpMode = true;
    this.otpForm.patchValue({ email });
    if (!this.isFirstLogin) {
      this.startResendCooldown();
    }
  }

  private startResendCooldown(): void {
    this.resendCooldown = 60;
    this.resendTimer = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) {
        clearInterval(this.resendTimer);
      }
    }, 1000);
  }

  toggleNewPassword(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  get passwordMismatch(): boolean {
    const newPassword = this.otpForm.get('newPassword')?.value;
    const confirmPassword = this.otpForm.get('confirmPassword')?.value;
    return newPassword !== confirmPassword && confirmPassword !== '';
  }

  isForgotFieldInvalid(fieldName: string): boolean {
    const field = this.forgotPasswordForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  isOtpFieldInvalid(fieldName: string): boolean {
    const field = this.otpForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private autoHideError(): void {
    setTimeout(() => {
      this.errorMessage = '';
    }, 10000);
  }

  ngOnDestroy(): void {
    if (this.resendTimer) {
      clearInterval(this.resendTimer);
    }
  }
} 

