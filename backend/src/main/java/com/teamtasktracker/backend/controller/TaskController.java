package com.teamtasktracker.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamtasktracker.backend.domain.enums.TaskPriority;
import com.teamtasktracker.backend.domain.enums.TaskStatus;
import com.teamtasktracker.backend.dto.common.PageResponse;
import com.teamtasktracker.backend.dto.task.AssignTaskRequest;
import com.teamtasktracker.backend.dto.task.CreateTaskRequest;
import com.teamtasktracker.backend.dto.task.TaskResponse;
import com.teamtasktracker.backend.dto.task.UpdateTaskRequest;
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
	public ResponseEntity<PageResponse<TaskResponse>> listTasks(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int limit,
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) TaskPriority priority,
			@RequestParam(required = false) Long assignee) {
		return ResponseEntity.ok(taskService.listTasks(page, limit, status, priority, assignee));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
		return ResponseEntity.ok(taskService.getTask(id));
	}

	@PostMapping
	public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TaskResponse> updateTask(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTaskRequest request) {
		return ResponseEntity.ok(taskService.updateTask(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/assign")
	public ResponseEntity<TaskResponse> assignTask(
			@PathVariable Long id,
			@Valid @RequestBody AssignTaskRequest request) {
		return ResponseEntity.ok(taskService.assignTask(id, request));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<TaskResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateTaskStatusRequest request) {
		return ResponseEntity.ok(taskService.updateStatus(id, request));
	}

}
