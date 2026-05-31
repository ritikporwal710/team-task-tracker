import type { TaskStatus } from "@/types";

const FORWARD: Partial<Record<TaskStatus, TaskStatus>> = {
  TODO: "IN_PROGRESS",
  IN_PROGRESS: "IN_REVIEW",
  IN_REVIEW: "DONE",
};

export function canTransition(from: TaskStatus, to: TaskStatus): boolean {
  if (from === to) {
    return true;
  }
  if (to === "BLOCKED") {
    return from !== "DONE";
  }
  if (from === "BLOCKED") {
    return to === "TODO";
  }
  return FORWARD[from] === to;
}

export function allowedTargets(from: TaskStatus): TaskStatus[] {
  const targets: TaskStatus[] = [from];
  if (from !== "DONE") {
    targets.push("BLOCKED");
  }
  if (from === "BLOCKED") {
    targets.push("TODO");
  }
  else if (FORWARD[from]) {
    targets.push(FORWARD[from]!);
  }
  return targets;
}
