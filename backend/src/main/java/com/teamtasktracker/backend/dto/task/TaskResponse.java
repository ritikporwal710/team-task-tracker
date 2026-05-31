package com.teamtasktracker.backend.dto.task;

import java.time.LocalDateTime;

import com.teamtasktracker.backend.domain.enums.TaskPriority;
import com.teamtasktracker.backend.domain.enums.TaskStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponse {

	private Long id;

	private String taskCode;

	private Long projectId;

	private String projectName;

	private String title;

	private String description;

	private TaskPriority priority;

	private TaskStatus status;

	private Long assigneeId;

	private String assigneeName;

	private LocalDateTime dueDate;

	private LocalDateTime createdAt;

}
