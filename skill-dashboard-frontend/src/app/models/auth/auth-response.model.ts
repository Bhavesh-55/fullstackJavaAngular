export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpiresIn: number;
  refreshTokenExpiresIn: number;
  userId: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}