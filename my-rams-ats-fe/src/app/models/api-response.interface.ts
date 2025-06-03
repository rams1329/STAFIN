export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: string;
}

export interface JobFunction {
  id: number;
  name: string;
  description?: string;
  active?: boolean;
  isActive?: boolean;
}

export interface JobType {
  id: number;
  name: string;
  description?: string;
  active?: boolean;
  isActive?: boolean;
}

export interface Location {
  id: number;
  name: string;
  description?: string;
  active?: boolean;
  isActive?: boolean;
}

export interface ExperienceLevel {
  id: number;
  name: string;
  description?: string;
  minYears?: number;
  maxYears?: number;
  active?: boolean;
  isActive?: boolean;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
  active?: boolean;
  isActive?: boolean;
} 