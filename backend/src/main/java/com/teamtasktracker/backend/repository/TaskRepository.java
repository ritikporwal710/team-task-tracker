package com.teamtasktracker.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	@Query("""
			SELECT t FROM Task t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignee
			WHERE t.organization.id = :organizationId AND t.active = true
			ORDER BY t.createdAt DESC
			""")
	List<Task> findActiveByOrganizationId(@Param("organizationId") Long organizationId);

	@Query("""
			SELECT t FROM Task t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignee
			WHERE t.organization.id = :organizationId
			  AND t.assignee.id = :assigneeId
			  AND t.active = true
			ORDER BY t.createdAt DESC
			""")
	List<Task> findActiveByOrganizationIdAndAssigneeId(
			@Param("organizationId") Long organizationId,
			@Param("assigneeId") Long assigneeId);

	@Query("""
			SELECT t FROM Task t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignee
			WHERE t.id = :id AND t.organization.id = :organizationId AND t.active = true
			""")
	Optional<Task> findActiveByIdAndOrganizationId(@Param("id") Long id,
			@Param("organizationId") Long organizationId);

}
