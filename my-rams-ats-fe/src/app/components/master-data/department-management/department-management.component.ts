import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { DepartmentService, Department } from '../../../services/department.service';
import { ToastService } from '../../../services/toast.service';
import { AuthService } from '../../../services/auth.service';

interface FilterOptions {
  search?: string;
  name?: string;
  description?: string;
  isActive?: boolean;
  createdDateFrom?: string;
  createdDateTo?: string;
  modifiedDateFrom?: string;
  modifiedDateTo?: string;
}

interface PaginationInfo {
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
  totalElements: number;
  totalPages: number;
}

@Component({
  selector: 'app-department-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './department-management.component.html',
  styleUrls: ['./department-management.component.css']
})
export class DepartmentManagementComponent implements OnInit {
  departmentForm: FormGroup;
  departments: Department[] = [];
  loading = false;
  isLoading = false;
  editingDepartment: Department | null = null;
  showAdvancedFilters = false;
  customPageSize: number = 10;

  filters: FilterOptions = {};

  pagination: PaginationInfo = {
    page: 0,
    size: 10,
    sort: 'name',
    direction: 'asc',
    totalElements: 0,
    totalPages: 0
  };

  constructor(
    private fb: FormBuilder,
    private departmentService: DepartmentService,
    private toastService: ToastService,
    private authService: AuthService
  ) {
    this.departmentForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.loading = true;
    
    console.log('ðŸ”„ Loading departments with filters:', this.filters);
    console.log('ðŸ”„ Pagination:', this.pagination);
    
    // Build query parameters
    const params: any = {
      page: this.pagination.page,
      size: this.pagination.size,
      sort: `${this.pagination.sort},${this.pagination.direction}`
    };

    // Add filter parameters
    Object.keys(this.filters).forEach(key => {
      const value = (this.filters as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        params[key] = value;
      }
    });
    
    this.departmentService.getDepartmentsWithFilters(params).subscribe({
      next: (response: any) => {
        console.log('âœ… Departments API response:', response);
        
        if (response && response.content) {
          this.departments = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        } else {
          console.warn('âš ï¸ Unexpected response format:', response);
          this.departments = [];
        }
        
        this.loading = false;
        
        console.log(`ðŸ“Š Loaded ${this.departments.length} departments successfully`);
      },
      error: (error) => {
        console.error('âŒ Error loading departments:', error);
        this.loading = false;
        this.toastService.error(
          error.message || 'Failed to load departments. Please try again.',
          'Load Failed'
        );
      }
    });
  }

  applyFilters(): void {
    this.pagination.page = 0; // Reset to first page when applying filters
    this.loadDepartments();
  }

  clearFilters(): void {
    this.filters = {};
    this.pagination.page = 0;
    this.loadDepartments();
  }

  sortBy(field: string): void {
    if (this.pagination.sort === field) {
      // Toggle direction if same field
      this.pagination.direction = this.pagination.direction === 'asc' ? 'desc' : 'asc';
    } else {
      // Set new field with default direction
      this.pagination.sort = field;
      this.pagination.direction = 'asc';
    }
    this.pagination.page = 0; // Reset to first page when sorting
    this.loadDepartments();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.pagination.totalPages) {
      this.pagination.page = page;
      this.loadDepartments();
    }
  }

  changePageSize(size: number): void {
    if (size && size > 0 && size <= 100) {
      this.pagination.size = size;
      this.pagination.page = 0; // Reset to first page when changing page size
      this.loadDepartments();
    }
  }

  onSubmit(): void {
    if (this.departmentForm.valid) {
      this.isLoading = true;

      const formData = this.departmentForm.value;

      if (this.editingDepartment) {
        // Update existing department
        this.departmentService.updateDepartment(this.editingDepartment.id!, formData).subscribe({
          next: (updatedDepartment) => {
            this.toastService.success('Department updated successfully', 'Success');
            this.resetForm();
            this.loadDepartments(); // Reload the list
            this.isLoading = false;
          },
          error: (error) => {
            this.toastService.error(
              error.message || 'Failed to update department',
              'Update Failed'
            );
            this.isLoading = false;
          }
        });
      } else {
        // Create new department
        this.departmentService.createDepartment(formData).subscribe({
          next: (newDepartment) => {
            this.toastService.success('Department created successfully', 'Success');
            this.resetForm();
            this.loadDepartments(); // Reload the list
            this.isLoading = false;
          },
          error: (error) => {
            this.toastService.error(
              error.message || 'Failed to create department',
              'Create Failed'
            );
            this.isLoading = false;
          }
        });
      }
    }
  }

  editDepartment(department: Department): void {
    this.editingDepartment = department;
    this.departmentForm.patchValue({
      name: department.name,
      description: department.description,
      isActive: department.isActive
    });
  }

  toggleStatus(department: Department): void {
    if (confirm(`Are you sure you want to ${department.isActive ? 'deactivate' : 'activate'} this department?`)) {
      this.departmentService.toggleDepartmentActive(department.id!).subscribe({
        next: (updatedDepartment) => {
          department.isActive = updatedDepartment.isActive;
          this.toastService.success(
            `Department ${department.isActive ? 'activated' : 'deactivated'} successfully`,
            'Success'
          );
        },
        error: (error) => {
          this.toastService.error(
            error.message || 'Failed to update department status',
            'Update Failed'
          );
        }
      });
    }
  }

  deleteDepartment(department: Department): void {
    if (confirm(`Are you sure you want to delete the department "${department.name}"? This action cannot be undone.`)) {
      this.departmentService.deleteDepartment(department.id!).subscribe({
        next: () => {
          this.toastService.success('Department deleted successfully', 'Success');
          this.loadDepartments(); // Reload the list
        },
        error: (error) => {
          this.toastService.error(
            error.message || 'Failed to delete department',
            'Delete Failed'
          );
        }
      });
    }
  }

  resetForm(): void {
    this.editingDepartment = null;
    this.departmentForm.reset({
      name: '',
      description: '',
      isActive: true
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.departmentForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
} 

