package com.teamtasktracker.backend.dto.task;

import com.teamtasktracker.backend.domain.enums.TaskStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskStatusRequest {

	@NotNull
	private TaskStatus status;

	private String remarks;

}
