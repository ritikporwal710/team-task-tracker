import { api } from "@/services/api";
import type { UserSummary } from "@/types";

export async function listMembers(): Promise<UserSummary[]> {
  const { data } = await api.get<UserSummary[]>("/users");
  return data;
}
