import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { JobService } from '../../../services/job.service';
import { LocationService, Location } from '../../../services/location.service';

interface FeaturedJob {
  id: number;
  jobTitle: string;
  requirementTitle: string;
  location: { name: string };
  jobType: { name: string };
  department?: { name: string };
  salaryMin?: number;
  salaryMax?: number;
  createdDate: string;
  isActive: boolean;
}

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing-page.component.html'
})
export class LandingPageComponent implements OnInit {
  featuredJobs: FeaturedJob[] = [];
  locations: Location[] = [];
  isLoading = true;
  errorMessage = '';
  stats = {
    totalJobs: 0,
    totalCompanies: 0,
    totalUsers: 0
  };

  constructor(
    private jobService: JobService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.loadFeaturedJobs();
    this.loadLocations();
    this.loadStats();
  }

  loadFeaturedJobs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.jobService.getPublicJobs().subscribe({
      next: (jobs: any[]) => {
        // Filter active jobs and take only first 6 for featured section
        this.featuredJobs = jobs
          .filter(job => job.isActive)
          .slice(0, 6)
          .map(job => ({
            id: job.id,
            jobTitle: job.jobTitle,
            requirementTitle: job.requirementTitle || job.jobDescription,
            location: job.location || { name: 'Remote' },
            jobType: job.jobType || { name: 'Full-time' },
            department: job.department,
            salaryMin: job.salaryMin,
            salaryMax: job.salaryMax,
            createdDate: job.createdDate,
            isActive: job.isActive
          }));
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading featured jobs:', error);
        this.errorMessage = error.message || 'Failed to load jobs. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  private loadLocations(): void {
    // Try to fetch from the public backend endpoint first
    this.locationService.getPublicActiveLocations().subscribe({
      next: (locations: Location[]) => {
        this.locations = [
          { id: 0, name: 'All Locations', isActive: true },
          ...locations.filter(loc => loc.isActive)
        ];
      },
      error: (error) => {
        console.log('Backend locations not available, using fallback locations:', error.message);
        // Fallback to hardcoded locations if backend is not available
        this.locations = [
          { id: 0, name: 'All Locations', isActive: true },
          { id: 1, name: 'Remote', isActive: true },
          { id: 2, name: 'New York, NY', isActive: true },
          { id: 3, name: 'San Francisco, CA', isActive: true },
          { id: 4, name: 'Los Angeles, CA', isActive: true },
          { id: 5, name: 'Chicago, IL', isActive: true },
          { id: 6, name: 'Austin, TX', isActive: true },
          { id: 7, name: 'Seattle, WA', isActive: true },
          { id: 8, name: 'Boston, MA', isActive: true },
          { id: 9, name: 'Denver, CO', isActive: true },
          { id: 10, name: 'Portland, OR', isActive: true }
        ];
      }
    });
  }

  private loadStats(): void {
    // Load statistics from backend or calculate from available data
    this.jobService.getPublicJobs().subscribe({
      next: (jobs: any[]) => {
        this.stats.totalJobs = jobs.filter(job => job.isActive).length;
        
        // Calculate unique departments as a proxy for companies
        const uniqueDepartments = new Set(jobs.map(job => job.department?.name).filter(Boolean));
        this.stats.totalCompanies = Math.max(uniqueDepartments.size, 10);
        
        // Mock user count - this would come from a real endpoint
        this.stats.totalUsers = Math.floor(this.stats.totalJobs * 12.5); // Rough estimate
      },
      error: (error) => {
        // Fallback stats
        this.stats = {
          totalJobs: 50,
          totalCompanies: 25,
          totalUsers: 500
        };
      }
    });
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
    } else if (diffDays < 30) {
      const weeks = Math.floor(diffDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    } else {
      const months = Math.floor(diffDays / 30);
      return `${months} month${months > 1 ? 's' : ''} ago`;
    }
  }
} 

