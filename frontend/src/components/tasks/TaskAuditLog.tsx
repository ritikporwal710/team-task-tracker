import { Badge } from "@/components/ui/badge";
import type { TaskStatus, TaskStatusHistoryEntry } from "@/types";

function formatStatus(status: TaskStatus | null) {
  if (!status) {
    return "—";
  }
  return status.replace(/_/g, " ");
}

function formatWhen(iso: string) {
  return new Date(iso).toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

interface TaskAuditLogProps {
  entries: TaskStatusHistoryEntry[];
  loading?: boolean;
}

export function TaskAuditLog({ entries, loading }: TaskAuditLogProps) {
  if (loading) {
    return (
      <p className="text-sm text-muted-foreground">Loading activity...</p>
    );
  }

  if (entries.length === 0) {
    return (
      <p className="rounded-lg border border-dashed bg-background px-4 py-6 text-center text-sm text-muted-foreground">
        No status changes yet. Move a task on the board to see activity here.
      </p>
    );
  }

  return (
    <ul className="space-y-2">
      {entries.map((entry) => (
        <li
          key={entry.id}
          className="flex flex-wrap items-center gap-x-3 gap-y-1 rounded-lg border bg-background px-4 py-3 text-sm"
        >
          <span className="font-medium text-muted-foreground tabular-nums">
            {formatWhen(entry.changedAt)}
          </span>
          <span className="text-muted-foreground">·</span>
          <span>
            <span className="font-medium">{entry.changedByName}</span>
            {" moved "}
            <span className="font-medium">{entry.taskCode}</span>
            {" ("}
            {entry.taskTitle}
            {") "}
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Badge variant="outline">{formatStatus(entry.oldStatus)}</Badge>
            <span className="text-muted-foreground">→</span>
            <Badge variant="secondary">{formatStatus(entry.newStatus)}</Badge>
          </span>
          {entry.remarks && (
            <span className="w-full text-xs text-muted-foreground">
              Note: {entry.remarks}
            </span>
          )}
        </li>
      ))}
    </ul>
  );
}
