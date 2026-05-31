package com.teamtasktracker.backend.dto.task;

import java.time.LocalDateTime;

import com.teamtasktracker.backend.domain.enums.TaskPriority;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {

	@Size(max = 255)
	private String title;

	private String description;

	private TaskPriority priority;

	@Future(message = "due_date must be a future date")
	private LocalDateTime dueDate;

}
