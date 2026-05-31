import { useMemo, useState } from "react";
import { GripVertical } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import type { Task, TaskStatus, UserSummary } from "@/types";

export const KANBAN_COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: "TODO", label: "To Do" },
  { status: "IN_PROGRESS", label: "In Progress" },
  { status: "IN_REVIEW", label: "In Review" },
  { status: "DONE", label: "Done" },
  { status: "BLOCKED", label: "Blocked" },
];

function priorityColor(priority: Task["priority"]) {
  switch (priority) {
    case "HIGH":
      return "text-destructive";
    case "LOW":
      return "text-muted-foreground";
    default:
      return "text-foreground";
  }
}

interface TaskKanbanBoardProps {
  tasks: Task[];
  members?: UserSummary[];
  canDrag: (task: Task) => boolean;
  canAssign?: boolean;
  onStatusChange: (taskId: number, status: TaskStatus) => Promise<void>;
  onAssign?: (taskId: number, assigneeId: number) => Promise<void>;
  emptyMessage?: string;
}

export function TaskKanbanBoard({
  tasks,
  members = [],
  canDrag,
  canAssign = false,
  onStatusChange,
  onAssign,
  emptyMessage = "No tasks to show.",
}: TaskKanbanBoardProps) {
  const [draggingId, setDraggingId] = useState<number | null>(null);
  const [overColumn, setOverColumn] = useState<TaskStatus | null>(null);
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  const tasksByStatus = useMemo(() => {
    const map = Object.fromEntries(
      KANBAN_COLUMNS.map((c) => [c.status, [] as Task[]]),
    ) as Record<TaskStatus, Task[]>;
    for (const task of tasks) {
      map[task.status]?.push(task);
    }
    return map;
  }, [tasks]);

  async function handleDrop(taskId: number, newStatus: TaskStatus) {
    const task = tasks.find((t) => t.id === taskId);
    if (!task || task.status === newStatus || !canDrag(task)) {
      return;
    }
    setUpdatingId(taskId);
    try {
      await onStatusChange(taskId, newStatus);
    } finally {
      setUpdatingId(null);
      setDraggingId(null);
      setOverColumn(null);
    }
  }

  if (tasks.length === 0) {
    return (
      <p className="rounded-lg border border-dashed bg-background px-4 py-8 text-center text-sm text-muted-foreground">
        {emptyMessage}
      </p>
    );
  }

  return (
    <div className="flex gap-3 overflow-x-auto pb-2">
      {KANBAN_COLUMNS.map(({ status, label }) => (
        <div
          key={status}
          className={cn(
            "flex w-72 shrink-0 flex-col rounded-xl border bg-muted/40 transition-colors",
            overColumn === status && "border-primary/50 bg-primary/5",
          )}
          onDragOver={(e) => {
            e.preventDefault();
            setOverColumn(status);
          }}
          onDragLeave={() => setOverColumn(null)}
          onDrop={(e) => {
            e.preventDefault();
            const taskId = Number(e.dataTransfer.getData("taskId"));
            if (taskId) {
              void handleDrop(taskId, status);
            }
          }}
        >
          <div className="flex items-center justify-between border-b px-3 py-2.5">
            <h3 className="text-sm font-medium">{label}</h3>
            <Badge variant="secondary" className="tabular-nums">
              {tasksByStatus[status].length}
            </Badge>
          </div>

          <div className="flex min-h-32 flex-1 flex-col gap-2 p-2">
            {tasksByStatus[status].map((task) => {
              const draggable = canDrag(task);
              const isUpdating = updatingId === task.id;

              return (
                <div
                  key={task.id}
                  draggable={draggable && !isUpdating}
                  onDragStart={(e) => {
                    if (!draggable) {
                      e.preventDefault();
                      return;
                    }
                    e.dataTransfer.setData("taskId", String(task.id));
                    e.dataTransfer.effectAllowed = "move";
                    setDraggingId(task.id);
                  }}
                  onDragEnd={() => {
                    setDraggingId(null);
                    setOverColumn(null);
                  }}
                  className={cn(
                    "rounded-lg border bg-background p-3 text-sm shadow-xs transition-opacity",
                    draggable && "cursor-grab active:cursor-grabbing",
                    draggingId === task.id && "opacity-50",
                    isUpdating && "pointer-events-none opacity-60",
                  )}
                >
                  <div className="mb-2 flex items-start gap-1">
                    {draggable && (
                      <GripVertical className="mt-0.5 size-4 shrink-0 text-muted-foreground" />
                    )}
                    <div className="min-w-0 flex-1">
                      <p className="font-medium leading-snug">{task.title}</p>
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        {task.taskCode} · {task.projectName}
                      </p>
                    </div>
                  </div>

                  <div className="mb-2 flex flex-wrap items-center gap-1.5">
                    <Badge variant="outline" className={priorityColor(task.priority)}>
                      {task.priority}
                    </Badge>
                  </div>

                  {canAssign && onAssign && members.length > 0 ? (
                    <div className="space-y-1">
                      <p className="text-xs font-medium text-muted-foreground">Assign to</p>
                      <Select
                        value={task.assigneeId ? String(task.assigneeId) : undefined}
                        onValueChange={(v) => void onAssign(task.id, Number(v))}
                      >
                        <SelectTrigger className="h-8 w-full text-xs">
                          <SelectValue placeholder="Select member" />
                        </SelectTrigger>
                        <SelectContent>
                          {members.map((m) => (
                            <SelectItem key={m.id} value={String(m.id)}>
                              {m.firstName} {m.lastName ?? ""}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  ) : (
                    <p className="text-xs text-muted-foreground">
                      {task.assigneeName ?? "Unassigned"}
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}
