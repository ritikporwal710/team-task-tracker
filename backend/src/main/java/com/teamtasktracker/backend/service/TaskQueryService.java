package com.teamtasktracker.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtasktracker.backend.domain.entity.Task;
import com.teamtasktracker.backend.domain.enums.TaskPriority;
import com.teamtasktracker.backend.domain.enums.TaskStatus;
import com.teamtasktracker.backend.dto.common.PageResponse;
import com.teamtasktracker.backend.dto.task.TaskResponse;
import com.teamtasktracker.backend.repository.TaskRepository;
import com.teamtasktracker.backend.repository.TaskSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskQueryService {

	private final TaskRepository taskRepository;

	@Transactional(readOnly = true)
	@Cacheable(
			value = TaskCacheService.TASK_LIST_CACHE,
			key = "'org:' + #organizationId + ':assignee:' + (#assigneeFilter != null ? #assigneeFilter : 'all') + ':page:' + #page + ':limit:' + #limit + ':status:' + (#status != null ? #status : '') + ':priority:' + (#priority != null ? #priority : '')")
	public PageResponse<TaskResponse> listTasks(
			Long organizationId,
			Long assigneeFilter,
			int page,
			int limit,
			TaskStatus status,
			TaskPriority priority) {
		List<Specification<Task>> specs = new ArrayList<>();
		specs.add(TaskSpecifications.forOrganization(organizationId));
		specs.add(TaskSpecifications.fetchDetails());

		if (assigneeFilter != null) {
			specs.add(TaskSpecifications.assignedTo(assigneeFilter));
		}
		if (status != null) {
			specs.add(TaskSpecifications.hasStatus(status));
		}
		if (priority != null) {
			specs.add(TaskSpecifications.hasPriority(priority));
		}

		Specification<Task> combined = specs.stream().reduce(Specification::and).orElse(null);
		Page<Task> result = taskRepository.findAll(
				combined,
				PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt")));

		return PageResponse.<TaskResponse>builder()
			.data(result.getContent().stream().map(this::toResponse).toList())
			.page(page)
			.limit(limit)
			.total(result.getTotalElements())
			.totalPages(result.getTotalPages())
			.build();
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
