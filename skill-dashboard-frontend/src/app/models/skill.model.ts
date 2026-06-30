export interface Skill {
  id: number;
  name: string;
  category: string;
  level: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SkillRequest {
  name: string;
  category: string;
  level: string;
  description?: string;
}

export interface SkillFilter {
  category?: string;
  level?: string;
  search?: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface SkillFilter {
  category?: string;
  level?: string;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}