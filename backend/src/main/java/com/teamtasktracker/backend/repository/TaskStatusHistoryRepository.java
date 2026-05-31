package com.teamtasktracker.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.TaskStatusHistory;

public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {

	@Query("""
			SELECT h FROM TaskStatusHistory h
			JOIN FETCH h.task t
			JOIN FETCH h.changedBy u
			WHERE t.organization.id = :organizationId
			  AND h.active = true
			  AND t.active = true
			ORDER BY h.createdAt DESC
			""")
	List<TaskStatusHistory> findRecentByOrganizationId(
			@Param("organizationId") Long organizationId,
			Pageable pageable);

	@Query("""
			SELECT h FROM TaskStatusHistory h
			JOIN FETCH h.task t
			JOIN FETCH h.changedBy u
			WHERE t.organization.id = :organizationId
			  AND t.assignee.id = :assigneeId
			  AND h.active = true
			  AND t.active = true
			ORDER BY h.createdAt DESC
			""")
	List<TaskStatusHistory> findRecentByOrganizationIdAndAssigneeId(
			@Param("organizationId") Long organizationId,
			@Param("assigneeId") Long assigneeId,
			Pageable pageable);

	@Query("""
			SELECT h FROM TaskStatusHistory h
			JOIN FETCH h.task t
			JOIN FETCH h.changedBy u
			WHERE t.id = :taskId
			  AND t.organization.id = :organizationId
			  AND h.active = true
			ORDER BY h.createdAt DESC
			""")
	List<TaskStatusHistory> findByTaskIdAndOrganizationId(
			@Param("taskId") Long taskId,
			@Param("organizationId") Long organizationId);

}
