// src/app/models/user.ts
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';  // ← Nur diese 2 Werte erlaubt
  createdAt: string;
}
