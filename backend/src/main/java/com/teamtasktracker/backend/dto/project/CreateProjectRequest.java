package com.teamtasktracker.backend.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {

	@NotBlank
	@Size(max = 255)
	private String name;

	private String description;

}
