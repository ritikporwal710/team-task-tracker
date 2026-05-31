package com.teamtasktracker.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamtasktracker.backend.dto.project.CreateProjectRequest;
import com.teamtasktracker.backend.dto.project.ProjectResponse;
import com.teamtasktracker.backend.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
	}

	@GetMapping
	public ResponseEntity<java.util.List<ProjectResponse>> listProjects() {
		return ResponseEntity.ok(projectService.listProjects());
	}

}
