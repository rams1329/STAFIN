import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { JobService, DashboardStats } from '../../services/job.service';
import { ContactService } from '../../services/contact.service';
import { JobPosting } from '../../models/job.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  dashboardStats: any[] = [];
  tableHeaders = ['Job Title', 'Department', 'Created Date', 'Status', 'Actions'];
  recentJobs: JobPosting[] = [];
  loading = true;
  error = '';

  constructor(
    private jobService: JobService,
    private contactService: ContactService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.loading = true;
    this.error = '';
    
    console.log('ðŸ”„ Loading dashboard data...');

    // Load dashboard stats
    this.jobService.getDashboardStats().subscribe({
      next: (stats: DashboardStats) => {
        console.log('âœ… Dashboard stats loaded:', stats);
        this.setupDashboardStats(stats);
      },
      error: (error: Error) => {
        console.error('âŒ Error loading dashboard stats:', error);
        // Setup default stats if API fails
        this.setupDashboardStats({
          totalJobs: 0,
          activeUsers: 0,
          unreadMessages: 0,
          applications: 0
        });
      }
    });

    // Load recent jobs
    this.jobService.getRecentJobs(5).subscribe({
      next: (jobs: JobPosting[]) => {
        console.log('âœ… Recent jobs loaded:', jobs);
        this.recentJobs = jobs;
        this.loading = false;
        
        // Force change detection to ensure UI updates
        this.changeDetectorRef.detectChanges();
        
        console.log(`ðŸ“Š Dashboard loaded with ${jobs.length} recent jobs`);
      },
      error: (error: Error) => {
        console.error('âŒ Error loading recent jobs:', error);
        this.error = error.message || 'Failed to load dashboard data';
        this.loading = false;
      }
    });
  }

  private setupDashboardStats(stats: DashboardStats): void {
    this.dashboardStats = [
      {
        label: 'Total Jobs',
        value: stats.totalJobs.toString(),
        change: '',
        changeClass: '',
        iconPath: 'M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2-2v2m8 0V6a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V8a2 2 0 012-2h8z'
      },
      {
        label: 'Active Users',
        value: stats.activeUsers.toString(),
        change: '',
        changeClass: '',
        iconPath: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z'
      },
      {
        label: 'Unread Messages',
        value: stats.unreadMessages.toString(),
        change: '',
        changeClass: '',
        iconPath: 'M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z'
      },
      {
        label: 'Applications',
        value: stats.applications.toString(),
        change: '',
        changeClass: '',
        iconPath: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
      }
    ];
  }

  refreshRecentJobs(): void {
    console.log('ðŸ”„ Refreshing recent jobs...');
    
    // Only show loading for recent jobs, not the entire dashboard
    const wasLoading = this.loading;
    if (!wasLoading) {
      this.loading = true;
    }
    
    this.error = '';

    this.jobService.getRecentJobs(5).subscribe({
      next: (jobs: JobPosting[]) => {
        console.log('âœ… Recent jobs refreshed:', jobs);
        this.recentJobs = jobs;
        
        // Force change detection to update the UI
        this.changeDetectorRef.detectChanges();
        
        if (!wasLoading) {
          this.loading = false;
        }
        
        console.log(`ðŸ“Š Updated recent jobs (${jobs.length} jobs)`);
      },
      error: (error: Error) => {
        console.error('âŒ Error refreshing recent jobs:', error);
        this.error = error.message || 'Failed to refresh recent jobs';
        
        if (!wasLoading) {
          this.loading = false;
        }
      }
    });
  }

  refreshAllData(): void {
    console.log('ðŸ”„ Refreshing all dashboard data...');
    this.loadDashboardData();
  }

  // Method to update a specific job's status if it exists in recent jobs
  updateJobStatusInRecentJobs(jobId: number, newStatus: boolean): void {
    const jobIndex = this.recentJobs.findIndex(job => job.id === jobId);
    if (jobIndex !== -1) {
      console.log(`ðŸ”„ Updating job status in dashboard for job ID ${jobId}: ${newStatus}`);
      
      // Create a new object to ensure change detection
      this.recentJobs[jobIndex] = {
        ...this.recentJobs[jobIndex],
        isActive: newStatus
      };
      
      // Force change detection
      this.changeDetectorRef.detectChanges();
      
      console.log(`âœ… Job status updated in dashboard recent jobs`);
    }
  }
} 

