export interface User {
  id: number;
  name: string;
  email: string;
  firstLogin: boolean;
  active: boolean;
  roles: Role[];
  isAdmin?: boolean;
  profileImage?: string;
  createdDate?: Date;
  modifiedDate?: Date;
}

export interface Role {
  id: number;
  name: string;
  description: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface OtpVerificationRequest {
  email: string;
  otp: string;
  newPassword: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
  expiresIn?: number;
}

export interface ContactMessage {
  id?: number;
  name: string;
  email: string;
  phoneNumber: string;
  phone?: string;
  message: string;
  subject?: string;
  read?: boolean;
  isRead?: boolean;
  createdAt?: Date;
  readAt?: Date;
  readBy?: string;
  repliedAt?: Date;
} 