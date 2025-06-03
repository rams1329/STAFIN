import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DropdownService } from '../../../services/dropdown.service';
import { AuthService } from '../../../services/auth.service';
import { Location } from '../../../models/api-response.interface';

@Component({
  selector: 'app-location-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './location-management.component.html'
})
export class LocationManagementComponent implements OnInit {
  locationForm: FormGroup;
  locations: Location[] = [];
  loading = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  editingLocation: Location | null = null;

  constructor(
    private fb: FormBuilder,
    private dropdownService: DropdownService,
    private authService: AuthService
  ) {
    this.locationForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadLocations();
  }

  loadLocations(): void {
    this.loading = true;
    
    this.dropdownService.getLocations().subscribe({
      next: (locations: Location[]) => {
        this.locations = Array.isArray(locations) ? locations : [];
        this.loading = false;
      },
      error: (error: Error) => {
        console.error('Error loading locations:', error);
        this.errorMessage = error.message || 'Failed to load locations';
        this.loading = false;
        
        // Check if it's an auth error
        if (error.message.includes('Unauthorized')) {
          console.log('Please login as admin user');
        }
      }
    });
  }

  onSubmit(): void {
    if (this.locationForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const formData = this.locationForm.value;

      if (this.editingLocation) {
        // Update existing location
        this.dropdownService.updateLocation(this.editingLocation.id, formData).subscribe({
          next: (response: Location) => {
            this.successMessage = 'Location updated successfully!';
            this.isLoading = false;
            this.loadLocations();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to update location.';
            this.isLoading = false;
          }
        });
      } else {
        // Create new location
        this.dropdownService.createLocation(formData).subscribe({
          next: (response: Location) => {
            this.successMessage = 'Location added successfully!';
            this.isLoading = false;
            this.loadLocations();
            this.resetForm();
          },
          error: (error: Error) => {
            this.errorMessage = error.message || 'Failed to add location.';
            this.isLoading = false;
          }
        });
      }
    }
  }

  editLocation(location: Location): void {
    this.editingLocation = location;
    this.locationForm.patchValue({
      name: location.name,
      description: location.description || '',
      active: location.active || location.isActive
    });
    this.clearMessages();
  }

  toggleStatus(location: Location): void {
    if (confirm(`Are you sure you want to ${(location.active || location.isActive) ? 'deactivate' : 'activate'} this location?`)) {
      this.dropdownService.toggleLocationActive(location.id).subscribe({
        next: (updatedItem: Location) => {
          // Update the item in the array
          const index = this.locations.findIndex(item => item.id === location.id);
          if (index !== -1) {
            this.locations[index] = updatedItem;
          }
          this.successMessage = 'Location status updated successfully!';
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to update location status.';
        }
      });
    }
  }

  deleteLocation(location: Location): void {
    if (confirm('Are you sure you want to delete this location? This action cannot be undone.')) {
      this.dropdownService.deleteLocation(location.id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Location deleted successfully!';
          this.loadLocations();
        },
        error: (error: Error) => {
          this.errorMessage = error.message || 'Failed to delete location.';
        }
      });
    }
  }

  resetForm(): void {
    this.locationForm.reset({
      name: '',
      description: '',
      active: true
    });
    this.editingLocation = null;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.resetForm();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.locationForm.get(fieldName);
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

