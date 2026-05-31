import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { TaskKanbanBoard } from "@/components/tasks/TaskKanbanBoard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { getErrorMessage } from "@/services/api";
import { createProject, listProjects } from "@/services/projects";
import {
  assignTask,
  createTask,
  listTasks,
  updateTaskStatus,
} from "@/services/tasks";
import { listMembers } from "@/services/users";
import { useAuthStore } from "@/stores/authStore";
import type { Project, Task, TaskPriority, TaskStatus, UserSummary } from "@/types";

const TASK_PRIORITIES: TaskPriority[] = ["LOW", "MEDIUM", "HIGH"];

export function DashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const hasRole = useAuthStore((s) => s.hasRole);

  const [projects, setProjects] = useState<Project[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [members, setMembers] = useState<UserSummary[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const [projectName, setProjectName] = useState("");
  const [projectDescription, setProjectDescription] = useState("");

  const [taskTitle, setTaskTitle] = useState("");
  const [taskDescription, setTaskDescription] = useState("");
  const [taskProjectId, setTaskProjectId] = useState<string>("");
  const [taskPriority, setTaskPriority] = useState<TaskPriority>("MEDIUM");
  const [taskAssigneeId, setTaskAssigneeId] = useState<string>("");

  const assignableMembers = useMemo(
    () => members.filter((m) => m.roles.includes("MEMBER")),
    [members],
  );

  const isManager = hasRole("MANAGER");
  const isMember = hasRole("MEMBER");
  const showKanban = isManager || isMember;

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const [projectData, taskPage, memberData] = await Promise.all([
        listProjects(),
        listTasks({ page: 1, limit: 100 }),
        listMembers(),
      ]);
      setProjects(projectData);
      setTasks(taskPage.data);
      setMembers(memberData);
      if (projectData.length > 0 && !taskProjectId) {
        setTaskProjectId(String(projectData[0].id));
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleCreateProject(e: React.FormEvent) {
    e.preventDefault();
    try {
      await createProject({
        name: projectName,
        description: projectDescription || undefined,
      });
      setProjectName("");
      setProjectDescription("");
      await loadData();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleCreateTask(e: React.FormEvent) {
    e.preventDefault();
    try {
      await createTask({
        projectId: Number(taskProjectId),
        title: taskTitle,
        description: taskDescription || undefined,
        priority: taskPriority,
        assigneeId: taskAssigneeId ? Number(taskAssigneeId) : undefined,
      });
      setTaskTitle("");
      setTaskDescription("");
      setTaskAssigneeId("");
      await loadData();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleStatusChange(taskId: number, status: TaskStatus) {
    setTasks((prev) =>
      prev.map((t) => (t.id === taskId ? { ...t, status } : t)),
    );
    try {
      await updateTaskStatus(taskId, status);
      await loadData();
    } catch (err) {
      setError(getErrorMessage(err));
      await loadData();
    }
  }

  async function handleAssign(taskId: number, assigneeId: number) {
    try {
      await assignTask(taskId, assigneeId);
      await loadData();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  function canDragTask(task: Task): boolean {
    if (isManager) {
      return true;
    }
    if (isMember) {
      return task.assigneeId === user?.id;
    }
    return false;
  }

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const roleLabel = user?.roles.join(", ") ?? "";

  const kanbanEmptyMessage = isMember
    ? "No tasks assigned to you yet. Ask a manager to assign work."
    : "No tasks yet. Create a task and assign it to a member.";

  return (
    <div className="min-h-svh bg-muted/30">
      <header className="border-b bg-background">
        <div className="mx-auto flex max-w-[1400px] items-center justify-between px-4 py-4">
          <div>
            <h1 className="text-lg font-semibold">Team Task Tracker</h1>
            <p className="text-sm text-muted-foreground">
              {user?.organizationName} · {user?.firstName} {user?.lastName}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Badge variant="secondary">{roleLabel}</Badge>
            <Button variant="outline" onClick={handleLogout}>
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-[1400px] space-y-6 p-4">
        {error && (
          <Card className="border-destructive/50">
            <CardContent className="pt-6 text-sm text-destructive">{error}</CardContent>
          </Card>
        )}

        {hasRole("ADMIN") && (
          <Card>
            <CardHeader>
              <CardTitle>Create project</CardTitle>
              <CardDescription>
                Admins can create projects for the organization.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleCreateProject} className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="projectName">Project name</Label>
                  <Input
                    id="projectName"
                    value={projectName}
                    onChange={(e) => setProjectName(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="projectDescription">Description</Label>
                  <Textarea
                    id="projectDescription"
                    value={projectDescription}
                    onChange={(e) => setProjectDescription(e.target.value)}
                    rows={2}
                  />
                </div>
                <Button type="submit">Create project</Button>
              </form>
            </CardContent>
          </Card>
        )}

        {isManager && (
          <Card>
            <CardHeader>
              <CardTitle>Create task</CardTitle>
              <CardDescription>
                Create tasks and assign them to team members.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleCreateTask} className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="taskTitle">Title</Label>
                  <Input
                    id="taskTitle"
                    value={taskTitle}
                    onChange={(e) => setTaskTitle(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="taskDescription">Description</Label>
                  <Textarea
                    id="taskDescription"
                    value={taskDescription}
                    onChange={(e) => setTaskDescription(e.target.value)}
                    rows={2}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Project</Label>
                  <Select value={taskProjectId} onValueChange={setTaskProjectId}>
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select project" />
                    </SelectTrigger>
                    <SelectContent>
                      {projects.map((p) => (
                        <SelectItem key={p.id} value={String(p.id)}>
                          {p.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Priority</Label>
                  <Select
                    value={taskPriority}
                    onValueChange={(v) => setTaskPriority(v as TaskPriority)}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {TASK_PRIORITIES.map((p) => (
                        <SelectItem key={p} value={p}>
                          {p}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2 md:col-span-2">
                  <Label>Assign to member</Label>
                  <Select value={taskAssigneeId} onValueChange={setTaskAssigneeId}>
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select member (optional)" />
                    </SelectTrigger>
                    <SelectContent>
                      {assignableMembers.map((m) => (
                        <SelectItem key={m.id} value={String(m.id)}>
                          {m.firstName} {m.lastName ?? ""}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <Button type="submit" disabled={!taskProjectId}>
                  Create task
                </Button>
              </form>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle>Projects</CardTitle>
            <CardDescription>
              {loading ? "Loading..." : `${projects.length} project(s)`}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {projects.length === 0 ? (
              <p className="text-sm text-muted-foreground">No projects yet.</p>
            ) : (
              <ul className="flex flex-wrap gap-2">
                {projects.map((p) => (
                  <li
                    key={p.id}
                    className="rounded-lg border bg-background px-3 py-2 text-sm"
                  >
                    <span className="font-medium">{p.name}</span>
                    {p.description && (
                      <span className="text-muted-foreground"> — {p.description}</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        {showKanban && (
          <Card>
            <CardHeader>
              <CardTitle>Task board</CardTitle>
              <CardDescription>
                {isManager
                  ? "Drag tasks between columns to update status. Assign members from each card."
                  : "Drag your assigned tasks between columns to update status."}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-sm text-muted-foreground">Loading board...</p>
              ) : (
                <TaskKanbanBoard
                  tasks={tasks}
                  members={assignableMembers}
                  canDrag={canDragTask}
                  canAssign={isManager}
                  onStatusChange={handleStatusChange}
                  onAssign={isManager ? handleAssign : undefined}
                  emptyMessage={kanbanEmptyMessage}
                />
              )}
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
}
