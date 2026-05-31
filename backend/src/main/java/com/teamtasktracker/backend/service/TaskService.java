package com.teamtasktracker.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtasktracker.backend.domain.entity.Task;
import com.teamtasktracker.backend.domain.entity.TaskStatusHistory;
import com.teamtasktracker.backend.domain.enums.TaskPriority;
import com.teamtasktracker.backend.domain.enums.TaskStatus;
import com.teamtasktracker.backend.dto.task.AssignTaskRequest;
import com.teamtasktracker.backend.dto.task.CreateTaskRequest;
import com.teamtasktracker.backend.dto.task.TaskResponse;
import com.teamtasktracker.backend.dto.task.UpdateTaskStatusRequest;
import com.teamtasktracker.backend.repository.OrganizationRepository;
import com.teamtasktracker.backend.repository.ProjectRepository;
import com.teamtasktracker.backend.repository.TaskRepository;
import com.teamtasktracker.backend.repository.TaskStatusHistoryRepository;
import com.teamtasktracker.backend.repository.UserRepository;
import com.teamtasktracker.backend.repository.UserRoleRepository;
import com.teamtasktracker.backend.security.SecurityUtils;
import com.teamtasktracker.backend.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;

	private final ProjectRepository projectRepository;

	private final OrganizationRepository organizationRepository;

	private final UserRepository userRepository;

	private final UserRoleRepository userRoleRepository;

	private final TaskStatusHistoryRepository taskStatusHistoryRepository;

	private final SecurityUtils securityUtils;

	@Transactional
	public TaskResponse createTask(CreateTaskRequest request) {
		UserPrincipal currentUser = securityUtils.currentUser();
		var organization = organizationRepository.findById(currentUser.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Organization not found"));

		var project = projectRepository.findById(request.getProjectId())
			.filter(p -> p.isActive() && p.getOrganization().getId().equals(currentUser.getOrganizationId()))
			.orElseThrow(() -> new IllegalArgumentException("Project not found"));

		Task task = new Task();
		task.setTaskCode(generateTaskCode());
		task.setOrganization(organization);
		task.setProject(project);
		task.setTitle(request.getTitle().trim());
		task.setDescription(request.getDescription());
		task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
		task.setStatus(TaskStatus.TODO);
		task.setCreatedBy(currentUser.getId());
		task.setUpdatedBy(currentUser.getId());

		if (request.getAssigneeId() != null) {
			task.setAssignee(resolveMemberAssignee(request.getAssigneeId(), currentUser.getOrganizationId()));
		}

		task = taskRepository.save(task);
		return toResponse(task);
	}

	@Transactional
	public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
		UserPrincipal currentUser = securityUtils.currentUser();
		Task task = findTaskForOrganization(taskId, currentUser.getOrganizationId());
		var assignee = resolveMemberAssignee(request.getAssigneeId(), currentUser.getOrganizationId());
		task.setAssignee(assignee);
		task.setUpdatedBy(currentUser.getId());
		task = taskRepository.save(task);
		return toResponse(task);
	}

	@Transactional
	public TaskResponse updateStatus(Long taskId, UpdateTaskStatusRequest request) {
		UserPrincipal currentUser = securityUtils.currentUser();
		Task task = findTaskForOrganization(taskId, currentUser.getOrganizationId());

		assertCanUpdateStatus(currentUser, task);

		TaskStatus oldStatus = task.getStatus();
		TaskStatus newStatus = request.getStatus();
		if (oldStatus == newStatus) {
			return toResponse(task);
		}

		task.setStatus(newStatus);
		task.setUpdatedBy(currentUser.getId());
		if (newStatus == TaskStatus.DONE) {
			task.setCompletedAt(LocalDateTime.now());
		}
		else {
			task.setCompletedAt(null);
		}
		task = taskRepository.save(task);

		var user = userRepository.findById(currentUser.getId()).orElseThrow();
		TaskStatusHistory history = new TaskStatusHistory();
		history.setTask(task);
		history.setOldStatus(oldStatus);
		history.setNewStatus(newStatus);
		history.setChangedBy(user);
		history.setRemarks(request.getRemarks());
		history.setCreatedBy(currentUser.getId());
		history.setUpdatedBy(currentUser.getId());
		taskStatusHistoryRepository.save(history);

		return toResponse(task);
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> listTasks() {
		UserPrincipal currentUser = securityUtils.currentUser();
		List<Task> tasks;
		if (currentUser.hasRole("MANAGER") || currentUser.hasRole("ADMIN")) {
			tasks = taskRepository.findActiveByOrganizationId(currentUser.getOrganizationId());
		}
		else {
			tasks = taskRepository.findActiveByOrganizationIdAndAssigneeId(
					currentUser.getOrganizationId(),
					currentUser.getId());
		}
		return tasks.stream()
			.map(this::toResponse)
			.toList();
	}

	private void assertCanUpdateStatus(UserPrincipal currentUser, Task task) {
		if (currentUser.hasRole("MANAGER") || currentUser.hasRole("ADMIN")) {
			return;
		}
		if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId())) {
			throw new IllegalArgumentException("You can only update tasks assigned to you");
		}
	}

	private Task findTaskForOrganization(Long taskId, Long organizationId) {
		return taskRepository.findActiveByIdAndOrganizationId(taskId, organizationId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found"));
	}

	private com.teamtasktracker.backend.domain.entity.User resolveMemberAssignee(Long assigneeId, Long organizationId) {
		var user = resolveAssignee(assigneeId, organizationId);
		boolean isMember = userRoleRepository.findActiveByUserId(user.getId()).stream()
			.anyMatch(ur -> "MEMBER".equalsIgnoreCase(ur.getRole().getName()));
		if (!isMember) {
			throw new IllegalArgumentException("Tasks can only be assigned to members");
		}
		return user;
	}

	private com.teamtasktracker.backend.domain.entity.User resolveAssignee(Long assigneeId, Long organizationId) {
		return userRepository.findById(assigneeId)
			.filter(u -> u.isActive() && u.getOrganization().getId().equals(organizationId))
			.orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
	}

	private String generateTaskCode() {
		return "NXT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private TaskResponse toResponse(Task task) {
		String assigneeName = null;
		Long assigneeId = null;
		if (task.getAssignee() != null) {
			assigneeId = task.getAssignee().getId();
			assigneeName = task.getAssignee().getFirstName()
					+ (task.getAssignee().getLastName() != null ? " " + task.getAssignee().getLastName() : "");
		}

		return TaskResponse.builder()
			.id(task.getId())
			.taskCode(task.getTaskCode())
			.projectId(task.getProject().getId())
			.projectName(task.getProject().getName())
			.title(task.getTitle())
			.description(task.getDescription())
			.priority(task.getPriority())
			.status(task.getStatus())
			.assigneeId(assigneeId)
			.assigneeName(assigneeName)
			.dueDate(task.getDueDate())
			.createdAt(task.getCreatedAt())
			.build();
	}

}
