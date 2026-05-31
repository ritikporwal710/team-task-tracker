package com.teamtasktracker.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamtasktracker.backend.domain.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

	Optional<Organization> findByNameIgnoreCaseAndActiveTrue(String name);

}
