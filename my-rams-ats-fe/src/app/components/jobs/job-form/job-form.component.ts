import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { JobService, JobPostingRequest } from '../../../services/job.service';
import { DropdownService } from '../../../services/dropdown.service';
import { JobFunction, JobType, Location, ExperienceLevel, Department } from '../../../models/api-response.interface';

// Custom validators
export class CustomValidators {
  static minMaxSalary(control: AbstractControl): ValidationErrors | null {
    const formGroup = control.parent;
    if (!formGroup) return null;

    const minSalary = formGroup.get('salaryMin')?.value;
    const maxSalary = formGroup.get('salaryMax')?.value;

    if (minSalary && maxSalary && parseFloat(minSalary) >= parseFloat(maxSalary)) {
      return { salaryRange: true };
    }
    return null;
  }

  static wordCount(minWords: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      
      const wordCount = control.value.trim().split(/\s+/).length;
      if (wordCount < minWords) {
        return { wordCount: { required: minWords, actual: wordCount } };
      }
      return null;
    };
  }
}

@Component({
  selector: 'app-job-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './job-form.component.html',
  styleUrls: ['./job-form.component.css']
})
export class JobFormComponent implements OnInit {
  jobForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  isEditMode = false;
  currentJobId: number | null = null;

  // Dropdown data
  departments: Department[] = [];
  jobFunctions: JobFunction[] = [];
  jobTypes: JobType[] = [];
  locations: Location[] = [];
  experienceLevels: ExperienceLevel[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private jobService: JobService,
    private dropdownService: DropdownService
  ) {
    this.jobForm = this.fb.group({
      requirementTitle: ['', [Validators.required, Validators.minLength(5)]],
      jobTitle: ['', [Validators.required, Validators.minLength(3)]],
      jobDesignation: ['', [Validators.required, Validators.minLength(3)]],
      jobDescription: ['', [Validators.required, CustomValidators.wordCount(20)]],
      jobFunctionId: ['', [Validators.required]],
      jobTypeId: ['', [Validators.required]],
      locationId: ['', [Validators.required]],
      departmentId: [''],
      experienceLevelId: [''],
      salaryMin: ['', [Validators.min(0)]],
      salaryMax: ['', [Validators.min(0), CustomValidators.minMaxSalary]],
      isActive: [true]
    });

    // Add cross-field validation for salary range
    this.jobForm.get('salaryMin')?.valueChanges.subscribe(() => {
      this.jobForm.get('salaryMax')?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    this.activatedRoute.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.currentJobId = +params['id'];
        this.loadJobForEdit(this.currentJobId);
      }
    });
    
    this.loadDropdownData();
  }

  private loadJobForEdit(jobId: number): void {
    this.isLoading = true;
    this.jobService.getJobById(jobId).subscribe({
      next: (job) => {
        this.populateForm(job);
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to load job details';
        this.isLoading = false;
      }
    });
  }

  private populateForm(job: any): void {
    this.jobForm.patchValue({
      requirementTitle: job.requirementTitle,
      jobTitle: job.jobTitle,
      jobDesignation: job.jobDesignation,
      jobDescription: job.jobDescription,
      jobFunctionId: job.jobFunction?.id || '',
      jobTypeId: job.jobType?.id || '',
      locationId: job.location?.id || '',
      departmentId: job.department?.id || '',
      experienceLevelId: job.experienceLevel?.id || '',
      salaryMin: job.salaryMin || '',
      salaryMax: job.salaryMax || '',
      isActive: job.isActive
    });
  }

  loadDropdownData(): void {
    // Check authentication first
    const token = localStorage.getItem('ats_admin_token');
    if (!token) {
      console.error('No authentication token found! Redirecting to login...');
      this.router.navigate(['/login']);
      return;
    }
    
    // Load dropdown data - use ACTIVE ONLY for form dropdowns
    this.dropdownService.getActiveJobFunctions().subscribe({
      next: (data) => {
        this.jobFunctions = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        console.error('Error loading job functions:', error);
        this.jobFunctions = [];
        this.handleAuthError(error);
      }
    });

    this.dropdownService.getActiveJobTypes().subscribe({
      next: (data) => {
        this.jobTypes = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        console.error('Error loading job types:', error);
        this.jobTypes = [];
        this.handleAuthError(error);
      }
    });

    this.dropdownService.getActiveLocations().subscribe({
      next: (data) => {
        this.locations = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        console.error('Error loading locations:', error);
        this.locations = [];
        this.handleAuthError(error);
      }
    });

    this.dropdownService.getActiveExperienceLevels().subscribe({
      next: (data) => {
        this.experienceLevels = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        console.error('Error loading experience levels:', error);
        this.experienceLevels = [];
        this.handleAuthError(error);
      }
    });

    this.dropdownService.getActiveDepartments().subscribe({
      next: (data) => {
        this.departments = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        this.departments = [];
        this.handleAuthError(error);
      }
    });
  }

  private handleAuthError(error: any): void {
    if (error.message && error.message.includes('Unauthorized')) {
      console.error('ðŸš¨ Authentication error detected - redirecting to login');
      this.errorMessage = 'Your session has expired. Please login again.';
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 2000);
    }
  }

  onSubmit(): void {
    if (this.jobForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formData = this.jobForm.value;
      
      // Convert empty strings to null for optional fields
      const jobRequest: JobPostingRequest = {
        requirementTitle: formData.requirementTitle,
        jobTitle: formData.jobTitle,
        jobDesignation: formData.jobDesignation,
        jobDescription: formData.jobDescription,
        jobFunctionId: parseInt(formData.jobFunctionId),
        jobTypeId: parseInt(formData.jobTypeId),
        locationId: parseInt(formData.locationId),
        departmentId: formData.departmentId ? parseInt(formData.departmentId) : undefined,
        experienceLevelId: formData.experienceLevelId ? parseInt(formData.experienceLevelId) : undefined,
        salaryMin: formData.salaryMin ? parseFloat(formData.salaryMin) : undefined,
        salaryMax: formData.salaryMax ? parseFloat(formData.salaryMax) : undefined,
        isActive: formData.isActive
      };

      console.log(this.isEditMode ? 'Updating job:' : 'Creating job:', jobRequest);

      if (this.isEditMode && this.currentJobId) {
        // Update existing job
        this.jobService.updateJob(this.currentJobId, jobRequest).subscribe({
          next: (response) => {
            this.successMessage = 'Job updated successfully!';
            this.isLoading = false;
            
            // Redirect to jobs list after 2 seconds
            setTimeout(() => {
              this.router.navigate(['/admin/jobs']);
            }, 2000);
          },
          error: (error) => {
            this.errorMessage = error.message || 'Failed to update job. Please try again.';
            this.isLoading = false;
          }
        });
      } else {
        // Create new job
        this.jobService.createJob(jobRequest).subscribe({
          next: (response) => {
            this.successMessage = 'Job created successfully!';
            this.isLoading = false;
            
            // Redirect to jobs list after 2 seconds
            setTimeout(() => {
              this.router.navigate(['/admin/jobs']);
            }, 2000);
          },
          error: (error) => {
            this.errorMessage = error.message || 'Failed to create job. Please try again.';
            this.isLoading = false;
          }
        });
      }
    } else {
      // Mark all fields as touched to show validation errors
      this.markAllFieldsAsTouched();
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  resetForm(): void {
    this.jobForm.reset({ isActive: true });
    this.errorMessage = '';
    this.successMessage = '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.jobForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  hasFieldError(fieldName: string, errorType: string): boolean {
    const field = this.jobForm.get(fieldName);
    return !!(field && field.errors?.[errorType] && (field.dirty || field.touched));
  }

  private markAllFieldsAsTouched(): void {
    Object.keys(this.jobForm.controls).forEach(key => {
      const control = this.jobForm.get(key);
      control?.markAsTouched();
    });
  }
} 

