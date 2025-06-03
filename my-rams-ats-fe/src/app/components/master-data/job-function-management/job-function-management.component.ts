import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DropdownService } from '../../../services/dropdown.service';
import { AuthService } from '../../../services/auth.service';
import { JobFunction } from '../../../models/api-response.interface';

@Component({
  selector: 'app-job-function-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './job-function-management.component.html'
})
export class JobFunctionManagementComponent implements OnInit {
  jobFunctionForm: FormGroup;
  jobFunctions: JobFunction[] = [];
  loading = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  editingJobFunction: JobFunction | null = null;

  constructor(
    private fb: FormBuilder,
    private dropdownService: DropdownService,
    private authService: AuthService
  ) {
    this.jobFunctionForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadJobFunctions();
  }

  loadJobFunctions(): void {
    this.loading = true;
    
    this.dropdownService.getJobFunctions().subscribe({
      next: (jobFunctions: JobFunction[]) => {
        this.jobFunctions = Array.isArray(jobFunctions) ? jobFunctions : [];
        this.loading = false;
      },
      error: (error: Error) => {
        console.error('Error loading job functions:', error);
        this.errorMessage = error.message || 'Failed to load job functions';
        this.loading = false;
        
        // Check if it's an auth error
        if (error.message.includes('Unauthorized')) {
          console.log('Please login as admin user');
        }
      }
    });
  }

  onSubmit(): void {
    if (this.jobFunctionForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const formData = this.jobFunctionForm.value;

      if (this.editingJobFunction) {
        // Update existing job function
        this.dropdownService.updateJobFunction(this.editingJobFunction.id, formData).subscribe({
          next: (response: JobFunction) => {
            this.successMessage = 'Job function updated successfully!';
            this.isLoading = false;
            this.loadJobFunctions();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to update job function.';
            this.isLoading = false;
          }
        });
      } else {
        // Create new job function
        this.dropdownService.createJobFunction(formData).subscribe({
          next: (response: JobFunction) => {
            this.successMessage = 'Job function added successfully!';
            this.isLoading = false;
            this.loadJobFunctions();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to add job function.';
            this.isLoading = false;
          }
        });
      }
    }
  }

  editJobFunction(jobFunction: JobFunction): void {
    this.editingJobFunction = jobFunction;
    this.jobFunctionForm.patchValue({
      name: jobFunction.name,
      description: jobFunction.description || '',
      active: jobFunction.active || jobFunction.isActive
    });
    this.clearMessages();
  }

  toggleStatus(jobFunction: JobFunction): void {
    if (confirm(`Are you sure you want to ${(jobFunction.active || jobFunction.isActive) ? 'deactivate' : 'activate'} this job function?`)) {
      this.dropdownService.toggleJobFunctionActive(jobFunction.id).subscribe({
        next: (updatedItem: JobFunction) => {
          // Update the item in the array
          const index = this.jobFunctions.findIndex(item => item.id === jobFunction.id);
          if (index !== -1) {
            this.jobFunctions[index] = updatedItem;
          }
          this.successMessage = 'Job function status updated successfully!';
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to update job function status.';
        }
      });
    }
  }

  deleteJobFunction(jobFunction: JobFunction): void {
    if (confirm('Are you sure you want to delete this job function? This action cannot be undone.')) {
      this.dropdownService.deleteJobFunction(jobFunction.id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Job function deleted successfully!';
          this.loadJobFunctions();
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to delete job function.';
        }
      });
    }
  }

  resetForm(): void {
    this.jobFunctionForm.reset({
      name: '',
      description: '',
      active: true
    });
    this.editingJobFunction = null;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.resetForm();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.jobFunctionForm.get(fieldName);
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

