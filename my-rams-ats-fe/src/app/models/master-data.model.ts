export interface Department {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface JobFunction {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface JobType {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface Location {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface ExperienceLevel {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface MasterDataRequest {
  name: string;
  description: string;
  active: boolean;
} 