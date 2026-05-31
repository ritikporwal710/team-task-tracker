package com.teamtasktracker.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamtasktracker.backend.domain.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.user u
			JOIN FETCH u.organization
			WHERE s.refreshTokenHash = :tokenHash
			  AND s.active = true
			  AND s.revokedAt IS NULL
			""")
	Optional<Session> findActiveByRefreshTokenHash(@Param("tokenHash") String tokenHash);

}
