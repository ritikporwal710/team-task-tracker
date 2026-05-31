package com.teamtasktracker.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

	@Query("""
			SELECT DISTINCT p.name FROM Permission p
			JOIN RolePermission rp ON rp.permission.id = p.id
			JOIN UserRole ur ON ur.role.id = rp.role.id
			WHERE ur.user.id = :userId
			  AND p.active = true
			  AND rp.active = true
			  AND ur.active = true
			""")
	List<String> findPermissionNamesByUserId(@Param("userId") Long userId);

}
