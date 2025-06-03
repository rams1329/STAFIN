import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DropdownService } from '../../../services/dropdown.service';
import { AuthService } from '../../../services/auth.service';
import { ExperienceLevel } from '../../../models/api-response.interface';

@Component({
  selector: 'app-experience-level-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './experience-level-management.component.html'
})
export class ExperienceLevelManagementComponent implements OnInit {
  experienceLevelForm: FormGroup;
  experienceLevels: ExperienceLevel[] = [];
  loading = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  editingExperienceLevel: ExperienceLevel | null = null;

  constructor(
    private fb: FormBuilder,
    private dropdownService: DropdownService,
    private authService: AuthService
  ) {
    this.experienceLevelForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      minYears: [null],
      maxYears: [null],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadExperienceLevels();
  }

  loadExperienceLevels(): void {
    this.loading = true;
    
    this.dropdownService.getExperienceLevels().subscribe({
      next: (experienceLevels: ExperienceLevel[]) => {
        this.experienceLevels = Array.isArray(experienceLevels) ? experienceLevels : [];
        this.loading = false;
      },
      error: (error: Error) => {
        console.error('Error loading experience levels:', error);
        this.errorMessage = error.message || 'Failed to load experience levels';
        this.loading = false;
        
        // Check if it's an auth error
        if (error.message.includes('Unauthorized')) {
          console.log('Please login as admin user');
        }
      }
    });
  }

  onSubmit(): void {
    if (this.experienceLevelForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const formData = this.experienceLevelForm.value;

      if (this.editingExperienceLevel) {
        // Update existing experience level
        this.dropdownService.updateExperienceLevel(this.editingExperienceLevel.id, formData).subscribe({
          next: (response: ExperienceLevel) => {
            this.successMessage = 'Experience level updated successfully!';
            this.isLoading = false;
            this.loadExperienceLevels();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to update experience level.';
            this.isLoading = false;
          }
        });
      } else {
        // Create new experience level
        this.dropdownService.createExperienceLevel(formData).subscribe({
          next: (response: ExperienceLevel) => {
            this.successMessage = 'Experience level added successfully!';
            this.isLoading = false;
            this.loadExperienceLevels();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to add experience level.';
            this.isLoading = false;
          }
        });
      }
    }
  }

  editExperienceLevel(experienceLevel: ExperienceLevel): void {
    this.editingExperienceLevel = experienceLevel;
    this.experienceLevelForm.patchValue({
      name: experienceLevel.name,
      description: experienceLevel.description || '',
      minYears: experienceLevel.minYears || null,
      maxYears: experienceLevel.maxYears || null,
      active: experienceLevel.active || experienceLevel.isActive
    });
    this.clearMessages();
  }

  toggleStatus(experienceLevel: ExperienceLevel): void {
    if (confirm(`Are you sure you want to ${(experienceLevel.active || experienceLevel.isActive) ? 'deactivate' : 'activate'} this experience level?`)) {
      this.dropdownService.toggleExperienceLevelActive(experienceLevel.id).subscribe({
        next: (updatedItem: ExperienceLevel) => {
          // Update the item in the array
          const index = this.experienceLevels.findIndex(item => item.id === experienceLevel.id);
          if (index !== -1) {
            this.experienceLevels[index] = updatedItem;
          }
          this.successMessage = 'Experience level status updated successfully!';
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to update experience level status.';
        }
      });
    }
  }

  deleteExperienceLevel(experienceLevel: ExperienceLevel): void {
    if (confirm('Are you sure you want to delete this experience level? This action cannot be undone.')) {
      this.dropdownService.deleteExperienceLevel(experienceLevel.id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Experience level deleted successfully!';
          this.loadExperienceLevels();
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to delete experience level.';
        }
      });
    }
  }

  resetForm(): void {
    this.experienceLevelForm.reset({
      name: '',
      description: '',
      minYears: null,
      maxYears: null,
      active: true
    });
    this.editingExperienceLevel = null;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.resetForm();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.experienceLevelForm.get(fieldName);
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

