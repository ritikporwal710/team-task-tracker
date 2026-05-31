package com.teamtasktracker.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@Query("""
			SELECT t FROM Task t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignee
			WHERE t.id = :id AND t.organization.id = :organizationId AND t.active = true
			""")
	Optional<Task> findActiveByIdAndOrganizationId(@Param("id") Long id,
			@Param("organizationId") Long organizationId);

	@Query("""
			SELECT DISTINCT t FROM Task t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignee
			WHERE t.id IN :ids
			""")
	List<Task> findByIdInWithDetails(@Param("ids") List<Long> ids);

}
