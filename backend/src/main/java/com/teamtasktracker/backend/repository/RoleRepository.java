package com.teamtasktracker.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamtasktracker.backend.domain.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByNameIgnoreCaseAndActiveTrue(String name);

}
