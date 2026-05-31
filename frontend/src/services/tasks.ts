import { api } from "@/services/api";
import type { PageResponse, Task, TaskPriority, TaskStatus, TaskStatusHistoryEntry } from "@/types";

export async function listTasks(params?: {
  page?: number;
  limit?: number;
  status?: TaskStatus;
  priority?: TaskPriority;
  assignee?: number;
}): Promise<PageResponse<Task>> {
  const { data } = await api.get<PageResponse<Task>>("/tasks", { params });
  return data;
}

export async function createTask(payload: {
  projectId: number;
  title: string;
  description?: string;
  priority?: TaskPriority;
  assigneeId?: number;
}): Promise<Task> {
  const { data } = await api.post<Task>("/tasks", payload);
  return data;
}

export async function assignTask(
  taskId: number,
  assigneeId: number,
): Promise<Task> {
  const { data } = await api.patch<Task>(`/tasks/${taskId}/assign`, {
    assigneeId,
  });
  return data;
}

export async function updateTaskStatus(
  taskId: number,
  status: TaskStatus,
  remarks?: string,
): Promise<Task> {
  const { data } = await api.patch<Task>(`/tasks/${taskId}/status`, {
    status,
    remarks,
  });
  return data;
}

export async function listStatusHistory(
  limit = 30,
): Promise<TaskStatusHistoryEntry[]> {
  const { data } = await api.get<TaskStatusHistoryEntry[]>("/tasks/status-history", {
    params: { limit },
  });
  return data;
}
