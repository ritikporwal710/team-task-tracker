import { api } from "@/services/api";
import type { Task, TaskPriority, TaskStatus } from "@/types";

export async function listTasks(): Promise<Task[]> {
  const { data } = await api.get<Task[]>("/tasks");
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
