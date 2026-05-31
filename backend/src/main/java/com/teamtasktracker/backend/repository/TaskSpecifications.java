package com.teamtasktracker.backend.repository;

import org.springframework.data.jpa.domain.Specification;

import com.teamtasktracker.backend.domain.entity.Task;
import com.teamtasktracker.backend.domain.enums.TaskPriority;
import com.teamtasktracker.backend.domain.enums.TaskStatus;

import jakarta.persistence.criteria.JoinType;

public final class TaskSpecifications {

	private TaskSpecifications() {
	}

	public static Specification<Task> forOrganization(Long organizationId) {
		return (root, query, cb) -> {
			query.distinct(true);
			return cb.and(
					cb.equal(root.get("organization").get("id"), organizationId),
					cb.isTrue(root.get("active")));
		};
	}

	public static Specification<Task> assignedTo(Long assigneeId) {
		return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
	}

	public static Specification<Task> hasStatus(TaskStatus status) {
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Task> hasPriority(TaskPriority priority) {
		return (root, query, cb) -> cb.equal(root.get("priority"), priority);
	}

	public static Specification<Task> fetchDetails() {
		return (root, query, cb) -> {
			if (query.getResultType() != Long.class && query.getResultType() != long.class) {
				root.fetch("project", JoinType.LEFT);
				root.fetch("assignee", JoinType.LEFT);
			}
			return cb.conjunction();
		};
	}

}
