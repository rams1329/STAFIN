import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../../services/job.service';
import { ToastService } from '../../../services/toast.service';
import { DialogService } from '../../../services/dialog.service';
import { JobPosting } from '../../../models/job.model';

interface FilterOptions {
  search?: string;
  title?: string;
  requirementTitle?: string;
  jobDesignation?: string;
  jobDescription?: string;
  jobFunctionId?: number;
  jobTypeId?: number;
  locationId?: number;
  departmentId?: number;
  experienceLevelId?: number;
  isActive?: boolean;
  createdByUserId?: number;
  createdDateFrom?: string;
  createdDateTo?: string;
  modifiedDateFrom?: string;
  modifiedDateTo?: string;
  salaryMinFrom?: number;
  salaryMinTo?: number;
  salaryMaxFrom?: number;
  salaryMaxTo?: number;
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
  selector: 'app-job-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './job-list.component.html',
  styleUrls: ['./job-list.component.css']
})
export class JobListComponent implements OnInit {
  jobs: JobPosting[] = [];
  loading = false;
  showAdvancedFilters = false;

  filters: FilterOptions = {};

  pagination: PaginationInfo = {
    page: 0,
    size: 10,
    sort: 'createdDate',
    direction: 'desc',
    totalElements: 0,
    totalPages: 0
  };

  constructor(
    private jobService: JobService,
    private toastService: ToastService,
    private dialogService: DialogService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading = true;
    
    console.log('ðŸ”„ Loading jobs with filters:', this.filters);
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
    
    this.jobService.getAllJobsWithFilters(params).subscribe({
      next: (response: any) => {
        console.log('âœ… Jobs API response:', response);
        
        if (response && response.content) {
          this.jobs = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        } else {
          console.warn('âš ï¸ Unexpected response format:', response);
          this.jobs = [];
        }
        
        this.loading = false;
        
        console.log(`ðŸ“Š Loaded ${this.jobs.length} jobs successfully`);
      },
      error: (error) => {
        console.error('âŒ Error loading jobs:', error);
        this.loading = false;
        this.toastService.error(
          error.message || 'Failed to load job listings. Please try again.',
          'Load Failed'
        );
      }
    });
  }

  applyFilters(): void {
    this.pagination.page = 0; // Reset to first page when applying filters
    this.loadJobs();
  }

  clearFilters(): void {
    this.filters = {};
    this.pagination.page = 0;
    this.loadJobs();
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
    this.loadJobs();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.pagination.totalPages) {
      this.pagination.page = page;
      this.loadJobs();
    }
  }

  changePageSize(size: number): void {
    this.pagination.size = size;
    this.pagination.page = 0; // Reset to first page when changing page size
    this.loadJobs();
  }

  toggleJobStatus(job: JobPosting): void {
    this.dialogService.confirm({
      title: 'Confirm Job Status Change',
      message: `Are you sure you want to ${job.isActive ? 'deactivate' : 'activate'} this job?`,
      type: 'warning',
      confirmText: 'Yes',
      cancelText: 'No'
    }).then((confirmed) => {
      if (confirmed) {
        const originalStatus = job.isActive;
        this.jobService.toggleJobStatus(job.id).subscribe({
          next: (updatedJob) => {
            console.log('âœ… Job status toggle response:', updatedJob);
            
            // Update the job in the local array to trigger change detection
            const jobIndex = this.jobs.findIndex(j => j.id === job.id);
            if (jobIndex !== -1) {
              // Create a new object to ensure change detection
              this.jobs[jobIndex] = {
                ...this.jobs[jobIndex],
                isActive: updatedJob.isActive !== undefined ? updatedJob.isActive : !originalStatus
              };
            }
            
            // Also update the passed job object
            job.isActive = updatedJob.isActive !== undefined ? updatedJob.isActive : !originalStatus;
            
            // Force change detection to update the UI
            this.changeDetectorRef.detectChanges();
            
            this.toastService.jobStatusChanged(job.jobTitle, job.isActive);
            
            console.log('ðŸ“Š Updated job status:', {
              jobId: job.id,
              newStatus: job.isActive,
              originalStatus: originalStatus
            });
          },
          error: (error: Error) => {
            console.error('âŒ Error toggling job status:', error);
            this.toastService.error(
              error.message || 'Failed to update job status. Please try again.',
              'Update Failed'
            );
          }
        });
      }
    });
  }

  deleteJob(job: JobPosting): void {
    this.dialogService.confirm({
      title: 'Confirm Job Deletion',
      message: `Are you sure you want to delete the job "${job.jobTitle}"? This action cannot be undone.`,
      type: 'danger',
      confirmText: 'Delete',
      cancelText: 'Cancel'
    }).then((confirmed) => {
      if (confirmed) {
        this.jobService.deleteJob(job.id).subscribe({
          next: () => {
            this.toastService.jobDeleted(job.jobTitle);
            this.loadJobs(); // Reload the jobs list
          },
          error: (error: Error) => {
            this.toastService.error(
              error.message || 'Failed to delete job. Please try again.',
              'Delete Failed'
            );
          }
        });
      }
    });
  }
}

