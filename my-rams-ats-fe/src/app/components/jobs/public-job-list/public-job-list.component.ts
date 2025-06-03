import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../../services/job.service';

interface FilterOptions {
  search?: string;
  title?: string;
  requirementTitle?: string;
  jobDesignation?: string;
  jobFunctionId?: number;
  jobTypeId?: number;
  locationId?: number;
  departmentId?: number;
  experienceLevelId?: number;
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
  selector: 'app-public-job-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './public-job-list.component.html'
})
export class PublicJobListComponent implements OnInit {
  jobs: any[] = [];
  isLoading = true;
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

  constructor(private jobService: JobService) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  private loadJobs(): void {
    this.isLoading = true;
    
    console.log('ðŸ”„ Loading public jobs with filters:', this.filters);
    console.log('ðŸ”„ Pagination:', this.pagination);
    
    // Build query parameters
    const params: any = {
      page: this.pagination.page,
      size: this.pagination.size,
      sort: `${this.pagination.sort},${this.pagination.direction}`,
      isActive: true // Only show active jobs to users
    };

    // Add filter parameters
    Object.keys(this.filters).forEach(key => {
      const value = (this.filters as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        params[key] = value;
      }
    });
    
    this.jobService.getPublicJobsWithFilters(params).subscribe({
      next: (response: any) => {
        console.log('âœ… Public jobs API response:', response);
        
        if (response && response.content) {
          this.jobs = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        } else if (Array.isArray(response)) {
          // Fallback for simple array response
          this.jobs = response;
          this.pagination.totalElements = response.length;
          this.pagination.totalPages = 1;
        } else {
          console.warn('âš ï¸ Unexpected response format:', response);
          this.jobs = [];
        }
        
        this.isLoading = false;
        
        console.log(`ðŸ“Š Loaded ${this.jobs.length} public jobs successfully`);
      },
      error: (error) => {
        console.error('âŒ Error loading public jobs:', error);
        this.isLoading = false;
        this.jobs = [];
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

  onSortChange(event: any): void {
    const value = event.target.value;
    const [sort, direction] = value.split(',');
    this.pagination.sort = sort;
    this.pagination.direction = direction as 'asc' | 'desc';
    this.pagination.page = 0;
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

  getTimeAgo(dateString: string): string {
    const now = new Date();
    const date = new Date(dateString);
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'Today';
    } else if (diffDays === 1) {
      return '1 day ago';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else {
      const weeks = Math.floor(diffDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    }
  }
} 

