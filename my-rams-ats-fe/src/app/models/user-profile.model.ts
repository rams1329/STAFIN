export interface UserBasicInfo {
  id?: number;
  mobileNumber?: string;
  gender?: Gender;
  dateOfBirth?: string;
  experienceYears?: number;
  experienceMonths?: number;
  annualSalaryLakhs?: number;
  annualSalaryThousands?: number;
  skills?: string[];
  jobFunction?: string;
  currentLocation?: string;
  preferredLocation?: string;
  resumeFilePath?: string;
  fullName?: string;
  email?: string;
}

export interface UserEducation {
  id?: number;
  qualification: string;
  course: string;
  specialization?: string;
  percentage?: number;
  collegeSchool?: string;
  universityBoard?: string;
  courseType?: CourseType;
  passingYear?: number;
}

export interface UserEmployment {
  id?: number;
  companyName: string;
  organizationType: OrganizationType;
  designation: string;
  workingSince: string;
  location?: string;
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}

export enum CourseType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  CORRESPONDENCE = 'CORRESPONDENCE'
}

export enum OrganizationType {
  CURRENT = 'CURRENT',
  PREVIOUS = 'PREVIOUS'
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
} 