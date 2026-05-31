import { api } from "@/services/api";
import type { AuthResponse, RoleName } from "@/types";

export interface RegisterPayload {
  firstName: string;
  lastName?: string;
  email: string;
  password: string;
  role: RoleName;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/register", payload);
  return data;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/login", payload);
  return data;
}
