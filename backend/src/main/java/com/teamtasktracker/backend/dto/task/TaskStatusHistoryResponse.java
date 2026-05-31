package com.teamtasktracker.backend.dto.task;

import java.time.LocalDateTime;

import com.teamtasktracker.backend.domain.enums.TaskStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskStatusHistoryResponse {

	private Long id;

	private Long taskId;

	private String taskCode;

	private String taskTitle;

	private TaskStatus oldStatus;

	private TaskStatus newStatus;

	private Long changedById;

	private String changedByName;

	private String remarks;

	private LocalDateTime changedAt;

}
