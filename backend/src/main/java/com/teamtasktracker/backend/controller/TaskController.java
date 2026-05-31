package com.teamtasktracker.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamtasktracker.backend.dto.task.AssignTaskRequest;
import com.teamtasktracker.backend.dto.task.CreateTaskRequest;
import com.teamtasktracker.backend.dto.task.TaskResponse;
import com.teamtasktracker.backend.dto.task.UpdateTaskStatusRequest;
import com.teamtasktracker.backend.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@GetMapping
	public ResponseEntity<List<TaskResponse>> listTasks() {
		return ResponseEntity.ok(taskService.listTasks());
	}

	@PostMapping
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<TaskResponse> assignTask(
			@PathVariable Long id,
			@Valid @RequestBody AssignTaskRequest request) {
		return ResponseEntity.ok(taskService.assignTask(id, request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('MEMBER', 'MANAGER')")
	public ResponseEntity<TaskResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTaskStatusRequest request) {
		return ResponseEntity.ok(taskService.updateStatus(id, request));
	}

}
