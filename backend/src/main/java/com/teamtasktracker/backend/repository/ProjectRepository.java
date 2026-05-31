package com.teamtasktracker.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	@Query("""
			SELECT p FROM Project p
			WHERE p.organization.id = :organizationId AND p.active = true
			ORDER BY p.createdAt DESC
			""")
	List<Project> findActiveByOrganizationId(@Param("organizationId") Long organizationId);

}
