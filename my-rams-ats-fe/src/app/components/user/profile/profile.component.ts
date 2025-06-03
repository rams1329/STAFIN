import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { AuthService, UpdateProfileRequest, PasswordResetRequest } from '../../../services/auth.service';
import { UserProfileService } from '../../../services/user-profile.service';
import { User } from '../../../models/user.model';
import { UserBasicInfo, UserEducation, UserEmployment, Gender, CourseType, OrganizationType } from '../../../models/user-profile.model';
import { ToastService } from '../../../services/toast.service';
import { DialogService } from '../../../services/dialog.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  basicInfoForm!: FormGroup;
  educationForm!: FormGroup;
  employmentForm!: FormGroup;
  
  user: User | null = null;
  userInitials = '';
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  isPasswordChangeMode = false;
  
  // New properties for extended profile
  basicInfo: UserBasicInfo | null = null;
  educationList: UserEducation[] = [];
  employmentList: UserEmployment[] = [];
  
  // Feature flags
  showExtendedProfile = false;
  
  // Form states
  isEditingEducation = false;
  isEditingEmployment = false;
  editingEducationId: number | null = null;
  editingEmploymentId: number | null = null;
  
  // File upload
  selectedResumeFile: File | null = null;
  
  // Enums for templates
  genderOptions = Object.values(Gender);
  courseTypeOptions = Object.values(CourseType);
  organizationTypeOptions = Object.values(OrganizationType);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userProfileService: UserProfileService,
    private toastService: ToastService,
    private dialogService: DialogService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.user = user;
        this.userInitials = this.getInitials(user.name);
        this.profileForm.patchValue({
          name: user.name,
          email: user.email
        });
        
        // Only load extended profile data after user is loaded
        this.loadExtendedProfileData();
      }
    });
  }

  private loadExtendedProfileData(): void {
    // Check authentication status first
    if (!this.authService.isAuthenticated()) {
      console.warn('âŒ User not authenticated, redirecting to login');
      this.authService.logout();
      return;
    }

    console.debug('ðŸ”„ Loading extended profile data for authenticated user');
    
    // First test if the new profile endpoints are available
    this.userProfileService.getUserBasicInfo().subscribe({
      next: (basicInfo) => {
        console.log('âœ… Successfully loaded basic profile info');
        // If we get here, the backend is available
        this.showExtendedProfile = true;
        this.basicInfo = basicInfo;
        this.basicInfoForm.patchValue({
          mobileNumber: basicInfo.mobileNumber || '',
          gender: basicInfo.gender || '',
          dateOfBirth: basicInfo.dateOfBirth || '',
          experienceYears: basicInfo.experienceYears || '',
          experienceMonths: basicInfo.experienceMonths || '',
          annualSalaryLakhs: basicInfo.annualSalaryLakhs || '',
          annualSalaryThousands: basicInfo.annualSalaryThousands || '',
          skills: basicInfo.skills || [],
          jobFunction: basicInfo.jobFunction || '',
          currentLocation: basicInfo.currentLocation || '',
          preferredLocation: basicInfo.preferredLocation || ''
        });
        
        // Load other sections
        this.loadEducation();
        this.loadEmployment();
      },
      error: (error) => {
        console.error('âŒ Error loading extended profile data:', error);
        
        if (error.status === 401) {
          console.warn('ðŸ” Authentication failed for profile API - redirecting to login');
          this.authService.logout();
          return;
        }
        
        console.log('ðŸ“ Extended profile features not available yet. Using basic profile only.');
        this.showExtendedProfile = false;
        // Don't show any error to user, just continue with basic profile
      }
    });
  }

  private loadEducation(): void {
    this.userProfileService.getUserEducation().subscribe({
      next: (education) => {
        this.educationList = education;
      },
      error: (error) => {
        console.error('Error loading education:', error);
        // Don't show error to user as this is extended functionality
        // Just log it and continue with basic profile functionality
      }
    });
  }

  private loadEmployment(): void {
    this.userProfileService.getUserEmployment().subscribe({
      next: (employment) => {
        this.employmentList = employment;
      },
      error: (error) => {
        console.error('Error loading employment:', error);
        // Don't show error to user as this is extended functionality
        // Just log it and continue with basic profile functionality
      }
    });
  }

  onSubmit(): void {
    console.log('ðŸ”„ Profile form submission triggered');
    console.log('ðŸ“‹ Form valid for current mode:', this.isFormValid);
    console.log('ðŸ” Password change mode:', this.isPasswordChangeMode);
    console.log('ðŸ” Password mismatch:', this.passwordMismatch);
    console.log('â³ Is loading:', this.isLoading);
    
    if (this.isFormValid && !this.passwordMismatch && !this.isLoading) {
      this.isLoading = true;
      this.clearMessages();

      const formValues = this.profileForm.value;
      const profileData: UpdateProfileRequest = {
        name: formValues.name
      };

      // Only add current password if changing password
      if (this.isPasswordChangeMode && formValues.currentPassword) {
        profileData.currentPassword = formValues.currentPassword;
      }

      // Add new password if provided
      if (formValues.newPassword) {
        profileData.newPassword = formValues.newPassword;
      }

      console.log('ðŸ“¤ Sending profile data:', profileData);

      this.authService.updateProfile(profileData).subscribe({
        next: (updatedUser) => {
          console.log('âœ… Profile updated successfully:', updatedUser);
          this.isLoading = false;
          const hasPasswordChange = formValues.newPassword;
          this.successMessage = hasPasswordChange ? 
            'Profile and password updated successfully!' : 
            'Profile updated successfully!';
          this.user = updatedUser;
          this.userInitials = this.getInitials(updatedUser.name);
          this.profileForm.patchValue({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          });
          this.isPasswordChangeMode = false; // Reset password change mode
          this.toastService.success(hasPasswordChange ? 'Profile and password updated successfully!' : 'Profile updated successfully!');
        },
        error: (error) => {
          console.error('âŒ Profile update failed:', error);
          this.isLoading = false;
          this.errorMessage = error.message || 'Failed to update profile. Please try again.';
          this.toastService.error(this.errorMessage);
        }
      });
    } else {
      console.log('âŒ Form submission blocked - validation failed');
      // Mark all fields as touched to show validation errors
      Object.keys(this.profileForm.controls).forEach(key => {
        this.profileForm.get(key)?.markAsTouched();
      });
    }
  }

  get isFormValid(): boolean {
    // Basic profile fields (name) must always be valid
    const nameValid = this.profileForm.get('name')?.valid;
    
    if (!this.isPasswordChangeMode) {
      // If not changing password, only name needs to be valid
      return !!nameValid;
    } else {
      // If changing password, all password-related fields must be valid
      const currentPasswordValid = this.profileForm.get('currentPassword')?.valid;
      const newPasswordValid = this.profileForm.get('newPassword')?.valid;
      const confirmPasswordValid = this.profileForm.get('confirmPassword')?.valid;
      
      return !!(nameValid && currentPasswordValid && newPasswordValid && confirmPasswordValid);
    }
  }

  get isFormInvalid(): boolean {
    return !this.isFormValid || this.passwordMismatch;
  }

  get passwordMismatch(): boolean {
    const newPassword = this.profileForm.get('newPassword')?.value;
    const confirmPassword = this.profileForm.get('confirmPassword')?.value;
    return newPassword && confirmPassword && newPassword !== confirmPassword;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  private getInitials(name: string): string {
    return name
      .split(' ')
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: [{value: '', disabled: true}],
      currentPassword: [''],
      newPassword: ['', [Validators.minLength(6)]],
      confirmPassword: ['']
    });

    this.basicInfoForm = this.fb.group({
      mobileNumber: ['', [
        Validators.pattern(/^[6-9]\d{9}$/), // 10 digits starting with 6-9
        Validators.minLength(10),
        Validators.maxLength(10)
      ]],
      gender: [''],
      dateOfBirth: [''],
      experienceYears: ['', [Validators.min(0)]],
      experienceMonths: ['', [Validators.min(0), Validators.max(11)]],
      annualSalaryLakhs: ['', [Validators.min(0)]],
      annualSalaryThousands: ['', [Validators.min(0), Validators.max(99)]],
      skills: [[]],
      jobFunction: [''],
      currentLocation: [''],
      preferredLocation: ['']
    });

    this.educationForm = this.fb.group({
      qualification: ['', Validators.required],
      course: ['', Validators.required],
      specialization: [''],
      percentage: ['', [Validators.min(0), Validators.max(100)]],
      collegeSchool: [''],
      universityBoard: [''],
      courseType: [''],
      passingYear: ['', [Validators.min(1950), Validators.max(this.getCurrentYear())]]
    });

    this.employmentForm = this.fb.group({
      companyName: ['', Validators.required],
      organizationType: ['', Validators.required],
      designation: ['', Validators.required],
      workingSince: ['', Validators.required],
      location: ['']
    });

    // Dynamic validation based on password change mode
    this.profileForm.get('newPassword')?.valueChanges.subscribe(value => {
      const confirmPasswordControl = this.profileForm.get('confirmPassword');
      const currentPasswordControl = this.profileForm.get('currentPassword');
      
      this.isPasswordChangeMode = !!value;
      
      if (value) {
        // If changing password, require current password and password confirmation
        confirmPasswordControl?.setValidators([Validators.required]);
        currentPasswordControl?.setValidators([Validators.required]);
      } else {
        // If not changing password, remove validators
        confirmPasswordControl?.clearValidators();
        currentPasswordControl?.clearValidators();
      }
      
      confirmPasswordControl?.updateValueAndValidity();
      currentPasswordControl?.updateValueAndValidity();
    });
  }

  // Basic Info Methods
  onSaveBasicInfo(): void {
    if (this.basicInfoForm.valid) {
      this.isLoading = true;
      const formData = this.basicInfoForm.value;
      
      // Convert empty strings to null for numeric fields
      const processedData = {
        ...formData,
        experienceYears: formData.experienceYears ? Number(formData.experienceYears) : null,
        experienceMonths: formData.experienceMonths ? Number(formData.experienceMonths) : null,
        annualSalaryLakhs: formData.annualSalaryLakhs ? Number(formData.annualSalaryLakhs) : null,
        annualSalaryThousands: formData.annualSalaryThousands ? Number(formData.annualSalaryThousands) : null
      };
      
      this.userProfileService.saveUserBasicInfo(processedData).subscribe({
        next: (savedInfo) => {
          this.basicInfo = savedInfo;
          this.isLoading = false;
          this.toastService.success('Basic information saved successfully!');
        },
        error: (error) => {
          this.isLoading = false;
          this.toastService.error('Failed to save basic information');
          console.error('Error saving basic info:', error);
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.basicInfoForm.controls).forEach(key => {
        this.basicInfoForm.get(key)?.markAsTouched();
      });
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedResumeFile = file;
    }
  }

  onUploadResume(): void {
    if (this.selectedResumeFile) {
      this.isLoading = true;
      this.userProfileService.uploadResume(this.selectedResumeFile).subscribe({
        next: (filePath) => {
          this.isLoading = false;
          this.toastService.success('Resume uploaded successfully!');
          this.loadExtendedProfileData(); // Reload to get updated file path
          this.selectedResumeFile = null;
        },
        error: (error) => {
          this.isLoading = false;
          this.toastService.error('Failed to upload resume');
          console.error('Error uploading resume:', error);
        }
      });
    }
  }

  // Education Methods
  onAddEducation(): void {
    this.isEditingEducation = true;
    this.editingEducationId = null;
    this.educationForm.reset();
  }

  onEditEducation(education: UserEducation): void {
    this.isEditingEducation = true;
    this.editingEducationId = education.id || null;
    this.educationForm.patchValue(education);
  }

  onSaveEducation(): void {
    if (this.educationForm.valid) {
      this.isLoading = true;
      const formData = this.educationForm.value;

      if (this.editingEducationId) {
        // Update existing education
        this.userProfileService.updateUserEducation(this.editingEducationId, formData).subscribe({
          next: () => {
            this.isLoading = false;
            this.isEditingEducation = false;
            this.toastService.success('Education updated successfully!');
            this.loadEducation();
          },
          error: (error) => {
            this.isLoading = false;
            this.toastService.error('Failed to update education');
            console.error('Error updating education:', error);
          }
        });
      } else {
        // Add new education
        this.userProfileService.saveUserEducation(formData).subscribe({
          next: () => {
            this.isLoading = false;
            this.isEditingEducation = false;
            this.toastService.success('Education added successfully!');
            this.loadEducation();
          },
          error: (error) => {
            this.isLoading = false;
            this.toastService.error('Failed to add education');
            console.error('Error adding education:', error);
          }
        });
      }
    }
  }

  onCancelEducation(): void {
    this.isEditingEducation = false;
    this.editingEducationId = null;
    this.educationForm.reset();
  }

  onDeleteEducation(educationId: number): void {
    this.dialogService.confirmDeleteEducation().then(confirmed => {
      if (confirmed) {
        this.userProfileService.deleteUserEducation(educationId).subscribe({
          next: () => {
            this.toastService.success('Education deleted successfully!');
            this.loadEducation();
          },
          error: (error) => {
            this.toastService.error('Failed to delete education');
            console.error('Error deleting education:', error);
          }
        });
      }
    });
  }

  // Employment Methods
  onAddEmployment(): void {
    this.isEditingEmployment = true;
    this.editingEmploymentId = null;
    this.employmentForm.reset();
  }

  onEditEmployment(employment: UserEmployment): void {
    this.isEditingEmployment = true;
    this.editingEmploymentId = employment.id || null;
    this.employmentForm.patchValue(employment);
  }

  onSaveEmployment(): void {
    if (this.employmentForm.valid) {
      this.isLoading = true;
      const formData = this.employmentForm.value;

      if (this.editingEmploymentId) {
        // Update existing employment
        this.userProfileService.updateUserEmployment(this.editingEmploymentId, formData).subscribe({
          next: () => {
            this.isLoading = false;
            this.isEditingEmployment = false;
            this.toastService.success('Employment updated successfully!');
            this.loadEmployment();
          },
          error: (error) => {
            this.isLoading = false;
            this.toastService.error('Failed to update employment');
            console.error('Error updating employment:', error);
          }
        });
      } else {
        // Add new employment
        this.userProfileService.saveUserEmployment(formData).subscribe({
          next: () => {
            this.isLoading = false;
            this.isEditingEmployment = false;
            this.toastService.success('Employment added successfully!');
            this.loadEmployment();
          },
          error: (error) => {
            this.isLoading = false;
            this.toastService.error('Failed to add employment');
            console.error('Error adding employment:', error);
          }
        });
      }
    }
  }

  onCancelEmployment(): void {
    this.isEditingEmployment = false;
    this.editingEmploymentId = null;
    this.employmentForm.reset();
  }

  onDeleteEmployment(employmentId: number): void {
    this.dialogService.confirmDeleteEmployment().then(confirmed => {
      if (confirmed) {
        this.userProfileService.deleteUserEmployment(employmentId).subscribe({
          next: () => {
            this.toastService.success('Employment deleted successfully!');
            this.loadEmployment();
          },
          error: (error) => {
            this.toastService.error('Failed to delete employment');
            console.error('Error deleting employment:', error);
          }
        });
      }
    });
  }

  getCurrentYear(): number {
    return new Date().getFullYear();
  }

  isBasicFieldInvalid(fieldName: string): boolean {
    const field = this.basicInfoForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
} 

