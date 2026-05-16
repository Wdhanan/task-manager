// src/app/models/auth-response.ts
export interface AuthResponse {
  token: string;      // ← Das JWT Token (Ausweis)
  type: string;       // "Bearer"
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}