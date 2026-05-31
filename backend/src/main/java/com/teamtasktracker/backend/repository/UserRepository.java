package com.teamtasktracker.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCaseAndActiveTrue(String email);

	boolean existsByEmailIgnoreCase(String email);

	@Query("""
			SELECT u FROM User u
			JOIN FETCH u.organization
			WHERE lower(u.email) = lower(:email) AND u.active = true
			""")
	Optional<User> findByEmailWithOrganization(@Param("email") String email);

	@Query("""
			SELECT u FROM User u
			WHERE u.organization.id = :organizationId AND u.active = true
			ORDER BY u.firstName, u.lastName
			""")
	List<User> findActiveByOrganizationId(@Param("organizationId") Long organizationId);

}
