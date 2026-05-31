export type RoleName = "ADMIN" | "MANAGER" | "MEMBER";

export type TaskPriority = "LOW" | "MEDIUM" | "HIGH";

export type TaskStatus =
  | "TODO"
  | "IN_PROGRESS"
  | "IN_REVIEW"
  | "DONE"
  | "BLOCKED";

export interface User {
  id: number;
  firstName: string;
  lastName?: string;
  email: string;
  organizationName: string;
  roles: RoleName[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInMs: number;
  user: User;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}

export interface Task {
  id: number;
  taskCode: string;
  projectId: number;
  projectName: string;
  title: string;
  description?: string;
  priority: TaskPriority;
  status: TaskStatus;
  assigneeId?: number;
  assigneeName?: string;
  dueDate?: string;
  createdAt: string;
}

export interface UserSummary {
  id: number;
  firstName: string;
  lastName?: string;
  email: string;
  roles: RoleName[];
}

export interface PageResponse<T> {
  data: T[];
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

export interface ApiError {
  status: number;
  code: string;
  message: string;
}
