package com.teamtasktracker.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

	@Query("""
			SELECT ur FROM UserRole ur
			JOIN FETCH ur.role
			WHERE ur.user.id = :userId AND ur.active = true
			""")
	List<UserRole> findActiveByUserId(@Param("userId") Long userId);

}
