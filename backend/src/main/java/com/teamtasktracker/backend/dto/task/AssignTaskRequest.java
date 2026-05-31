package com.teamtasktracker.backend.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignTaskRequest {

	@NotNull
	private Long assigneeId;

}
