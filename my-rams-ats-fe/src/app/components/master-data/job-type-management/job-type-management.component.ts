import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DropdownService } from '../../../services/dropdown.service';
import { AuthService } from '../../../services/auth.service';
import { JobType } from '../../../models/api-response.interface';

@Component({
  selector: 'app-job-type-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './job-type-management.component.html'
})
export class JobTypeManagementComponent implements OnInit {
  jobTypeForm: FormGroup;
  jobTypes: JobType[] = [];
  loading = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  editingJobType: JobType | null = null;

  constructor(
    private fb: FormBuilder,
    private dropdownService: DropdownService,
    private authService: AuthService
  ) {
    this.jobTypeForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadJobTypes();
  }

  loadJobTypes(): void {
    this.loading = true;
    
    this.dropdownService.getJobTypes().subscribe({
      next: (jobTypes: JobType[]) => {
        this.jobTypes = Array.isArray(jobTypes) ? jobTypes : [];
        this.loading = false;
      },
      error: (error: Error) => {
        console.error('Error loading job types:', error);
        this.errorMessage = error.message || 'Failed to load job types';
        this.loading = false;
        
        // Check if it's an auth error
        if (error.message.includes('Unauthorized')) {
          console.log('Please login as admin user');
        }
      }
    });
  }

  onSubmit(): void {
    if (this.jobTypeForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const formData = this.jobTypeForm.value;

      if (this.editingJobType) {
        // Update existing job type
        this.dropdownService.updateJobType(this.editingJobType.id, formData).subscribe({
          next: (response: JobType) => {
            this.successMessage = 'Job type updated successfully!';
            this.isLoading = false;
            this.loadJobTypes();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to update job type.';
            this.isLoading = false;
          }
        });
      } else {
        // Create new job type
        this.dropdownService.createJobType(formData).subscribe({
          next: (response: JobType) => {
            this.successMessage = 'Job type added successfully!';
            this.isLoading = false;
            this.loadJobTypes();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to add job type.';
            this.isLoading = false;
          }
        });
      }
    }
  }

  editJobType(jobType: JobType): void {
    this.editingJobType = jobType;
    this.jobTypeForm.patchValue({
      name: jobType.name,
      description: jobType.description || '',
      active: jobType.active || jobType.isActive
    });
    this.clearMessages();
  }

  toggleStatus(jobType: JobType): void {
    if (confirm(`Are you sure you want to ${(jobType.active || jobType.isActive) ? 'deactivate' : 'activate'} this job type?`)) {
      this.dropdownService.toggleJobTypeActive(jobType.id).subscribe({
        next: (updatedItem: JobType) => {
          // Update the item in the array
          const index = this.jobTypes.findIndex(item => item.id === jobType.id);
          if (index !== -1) {
            this.jobTypes[index] = updatedItem;
          }
          this.successMessage = 'Job type status updated successfully!';
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to update job type status.';
        }
      });
    }
  }

  deleteJobType(jobType: JobType): void {
    if (confirm('Are you sure you want to delete this job type? This action cannot be undone.')) {
      this.dropdownService.deleteJobType(jobType.id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Job type deleted successfully!';
          this.loadJobTypes();
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to delete job type.';
        }
      });
    }
  }

  resetForm(): void {
    this.jobTypeForm.reset({
      name: '',
      description: '',
      active: true
    });
    this.editingJobType = null;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.resetForm();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.jobTypeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private handleError(error: any): void {
    console.error('Error occurred:', error);
    this.errorMessage = error.message || 'An error occurred. Please try again.';
    this.loading = false;
    
    // Clear error message after 10 seconds
    setTimeout(() => {
      this.errorMessage = '';
    }, 10000);
  }
}

