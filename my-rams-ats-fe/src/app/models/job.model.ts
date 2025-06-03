import { Department, JobFunction, JobType, Location, ExperienceLevel } from './master-data.model';

export interface JobPosting {
  id: number;
  requirementTitle: string;
  jobTitle: string;
  jobDesignation: string;
  jobDescription: string;
  jobFunction: JobFunction;
  jobType: JobType;
  location: Location;
  department: Department | null;
  experienceLevel: ExperienceLevel | null;
  salaryMin: number;
  salaryMax: number;
  isActive: boolean;
  createdDate: Date;
  modifiedDate: Date;
  applicationCount?: number;
}

export interface JobPostingRequest {
  requirementTitle: string;
  jobTitle: string;
  jobDesignation: string;
  jobDescription: string;
  jobFunctionId: number;
  jobTypeId: number;
  locationId: number;
  departmentId: number;
  experienceLevelId: number;
  salaryMin: number;
  salaryMax: number;
  isActive: boolean;
}

export interface JobSearchParams {
  page?: number;
  size?: number;
  title?: string;
  jobFunctionId?: number;
  locationId?: number;
  departmentId?: number;
  isActive?: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
} 