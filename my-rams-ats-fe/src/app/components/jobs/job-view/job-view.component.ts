import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { JobService } from '../../../services/job.service';
import { JobPosting } from '../../../models/job.model';

@Component({
  selector: 'app-job-view',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './job-view.component.html'
})
export class JobViewComponent implements OnInit {
  job: JobPosting | null = null;
  loading = false;
  errorMessage = '';
  jobId: number | null = null;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private jobService: JobService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      if (params['id']) {
        this.jobId = +params['id'];
        this.loadJob(this.jobId);
      }
    });
  }

  private loadJob(jobId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.jobService.getJobById(jobId).subscribe({
      next: (job) => {
        this.job = job;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to load job details';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/jobs']);
  }

  toggleJobStatus(): void {
    if (!this.job) return;

    const action = this.job.isActive ? 'deactivate' : 'activate';
    const originalStatus = this.job.isActive;
    
    if (confirm(`Are you sure you want to ${action} this job?`)) {
      this.jobService.toggleJobStatus(this.job.id).subscribe({
        next: (updatedJob) => {
          console.log('âœ… Job status toggle response:', updatedJob);
          
          if (this.job) {
            // Update the job status with fallback logic
            this.job.isActive = updatedJob.isActive !== undefined ? updatedJob.isActive : !originalStatus;
            this.changeDetectorRef.detectChanges();
            
            console.log('ðŸ“Š Updated job status in view:', {
              jobId: this.job.id,
              newStatus: this.job.isActive,
              originalStatus: originalStatus
            });
          }
        },
        error: (error) => {
          console.error('âŒ Error toggling job status:', error);
          this.errorMessage = error.message || 'Failed to update job status';
        }
      });
    }
  }
} 

