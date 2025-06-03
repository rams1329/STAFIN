import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { JobService } from '../../../services/job.service';

@Component({
  selector: 'app-public-job-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './public-job-view.component.html'
})
export class PublicJobViewComponent implements OnInit {
  job: any = null;
  isLoading = true;
  jobId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private jobService: JobService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.jobId = +params['id'];
      if (this.jobId) {
        this.loadJob();
      } else {
        this.router.navigate(['/jobs']);
      }
    });
  }

  private loadJob(): void {
    this.isLoading = true;
    console.log(`ðŸ”„ Loading public job with ID: ${this.jobId}`);
    
    // Try the regular job endpoint first (which might have more complete data)
    this.jobService.getFreshJobById(this.jobId).subscribe({
      next: (job) => {
        console.log('âœ… Public job loaded successfully:', job);
        this.job = job;
        this.isLoading = false;
      },
      error: (error) => {
        console.warn('âš ï¸ Regular job endpoint failed, trying public endpoint:', error);
        
        // Fallback to public endpoint
        this.jobService.getFreshPublicJobById(this.jobId).subscribe({
          next: (job) => {
            console.log('âœ… Public job loaded via fallback:', job);
            this.job = job;
            this.isLoading = false;
          },
          error: (fallbackError) => {
            console.error('âŒ Both endpoints failed:', fallbackError);
            this.job = null;
            this.isLoading = false;
          }
        });
      }
    });
  }

  // Helper methods to safely extract data from job object
  getJobTypeName(job: any): string {
    if (!job) return 'Full-time';
    
    // Handle different API response formats
    if (job.jobType && typeof job.jobType === 'object' && job.jobType.name) {
      return job.jobType.name;
    } else if (job.jobType && typeof job.jobType === 'string' && job.jobType !== 'type') {
      return job.jobType;
    } else if (job.employmentType) {
      return job.employmentType;
    }
    
    return 'Full-time';
  }

  getLocationName(job: any): string {
    if (!job) return 'Remote';
    
    if (job.location && typeof job.location === 'object' && job.location.name) {
      return job.location.name;
    } else if (job.location && typeof job.location === 'string') {
      return job.location;
    }
    
    return 'Remote';
  }

  getDepartmentName(job: any): string {
    if (!job) return '';
    
    if (job.department && typeof job.department === 'object' && job.department.name) {
      return job.department.name;
    } else if (job.department && typeof job.department === 'string') {
      return job.department;
    }
    
    return '';
  }

  getJobFunctionName(job: any): string {
    if (!job) return '';
    
    if (job.jobFunction && typeof job.jobFunction === 'object' && job.jobFunction.name) {
      return job.jobFunction.name;
    } else if (job.jobFunction && typeof job.jobFunction === 'string') {
      return job.jobFunction;
    }
    
    return '';
  }

  getExperienceLevelName(job: any): string {
    if (!job) return '';
    
    if (job.experienceLevel && typeof job.experienceLevel === 'object' && job.experienceLevel.name) {
      return job.experienceLevel.name;
    } else if (job.experienceLevel && typeof job.experienceLevel === 'string') {
      return job.experienceLevel;
    }
    
    return '';
  }

  getJobStatus(job: any): boolean {
    if (!job) return false;
    
    // Handle different ways the status might be represented
    if (typeof job.isActive === 'boolean') {
      return job.isActive;
    } else if (typeof job.isActive === 'string') {
      return job.isActive.toLowerCase() === 'true' || job.isActive.toLowerCase() === 'active';
    } else if (job.status) {
      return job.status.toLowerCase() === 'active';
    }
    
    return false;
  }

  getTimeAgo(dateString: string): string {
    const now = new Date();
    const date = new Date(dateString);
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'today';
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

