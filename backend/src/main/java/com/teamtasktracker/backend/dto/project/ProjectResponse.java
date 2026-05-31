package com.teamtasktracker.backend.dto.project;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectResponse {

	private Long id;

	private String name;

	private String description;

	private LocalDateTime createdAt;

}
