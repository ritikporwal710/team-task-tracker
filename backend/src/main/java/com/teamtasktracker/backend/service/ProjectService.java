package com.teamtasktracker.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtasktracker.backend.domain.entity.Project;
import com.teamtasktracker.backend.dto.project.CreateProjectRequest;
import com.teamtasktracker.backend.dto.project.ProjectResponse;
import com.teamtasktracker.backend.repository.OrganizationRepository;
import com.teamtasktracker.backend.repository.ProjectRepository;
import com.teamtasktracker.backend.security.SecurityUtils;
import com.teamtasktracker.backend.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;

	private final OrganizationRepository organizationRepository;

	private final SecurityUtils securityUtils;

	@Transactional
	public ProjectResponse createProject(CreateProjectRequest request) {
		UserPrincipal currentUser = securityUtils.currentUser();
		var organization = organizationRepository.findById(currentUser.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Organization not found"));

		Project project = new Project();
		project.setOrganization(organization);
		project.setName(request.getName().trim());
		project.setDescription(request.getDescription());
		project.setCreatedBy(currentUser.getId());
		project.setUpdatedBy(currentUser.getId());
		project = projectRepository.save(project);

		return toResponse(project);
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> listProjects() {
		UserPrincipal currentUser = securityUtils.currentUser();
		return projectRepository.findActiveByOrganizationId(currentUser.getOrganizationId()).stream()
			.map(this::toResponse)
			.toList();
	}

	private ProjectResponse toResponse(Project project) {
		return ProjectResponse.builder()
			.id(project.getId())
			.name(project.getName())
			.description(project.getDescription())
			.createdAt(project.getCreatedAt())
			.build();
	}

}
