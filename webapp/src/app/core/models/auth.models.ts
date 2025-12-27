export enum UserRole {
    GUEST = 'GUEST',
    STUDENT = 'STUDENT',
    TEACHER = 'TEACHER'
}

export interface User {
    id: number;
    username: string;
    email: string;
    role: UserRole;
    createdAt: string;
    lastLogin: string | null;
    active: boolean;
}

export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}