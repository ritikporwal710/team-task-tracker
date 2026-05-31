package com.teamtasktracker.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtasktracker.backend.domain.entity.Organization;
import com.teamtasktracker.backend.domain.entity.Session;
import com.teamtasktracker.backend.domain.entity.User;
import com.teamtasktracker.backend.domain.entity.UserRole;
import com.teamtasktracker.backend.dto.auth.AuthResponse;
import com.teamtasktracker.backend.dto.auth.LoginRequest;
import com.teamtasktracker.backend.dto.auth.RegisterRequest;
import com.teamtasktracker.backend.repository.OrganizationRepository;
import com.teamtasktracker.backend.repository.RoleRepository;
import com.teamtasktracker.backend.repository.SessionRepository;
import com.teamtasktracker.backend.repository.UserRepository;
import com.teamtasktracker.backend.repository.UserRoleRepository;
import com.teamtasktracker.backend.security.JwtService;
import com.teamtasktracker.backend.security.TokenHasher;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	private final OrganizationRepository organizationRepository;

	private final RoleRepository roleRepository;

	private final UserRoleRepository userRoleRepository;

	private final SessionRepository sessionRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	private final TokenHasher tokenHasher;

	@Value("${app.default-organization}")
	private String defaultOrganizationName;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			throw new IllegalArgumentException("Email is already registered");
		}

		Organization organization = organizationRepository
			.findByNameIgnoreCaseAndActiveTrue(defaultOrganizationName)
			.orElseGet(() -> {
				Organization org = new Organization();
				org.setName(defaultOrganizationName);
				return organizationRepository.save(org);
			});

		var role = roleRepository.findByNameIgnoreCaseAndActiveTrue(request.getRole().name())
			.orElseThrow(() -> new IllegalArgumentException("Invalid role"));

		User user = new User();
		user.setOrganization(organization);
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user = userRepository.save(user);

		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(role);
		userRole.setCreatedBy(user.getId());
		userRole.setUpdatedBy(user.getId());
		userRoleRepository.save(userRole);

		return buildAuthResponse(user, List.of(role.getName()));
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailWithOrganization(request.getEmail().trim().toLowerCase())
			.orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		List<String> roles = userRoleRepository.findActiveByUserId(user.getId()).stream()
			.map(ur -> ur.getRole().getName())
			.toList();

		return buildAuthResponse(user, roles);
	}

	@Transactional
	public AuthResponse refresh(String refreshToken) {
		Claims claims = jwtService.parseToken(refreshToken);
		if (!jwtService.isRefreshToken(claims)) {
			throw new IllegalArgumentException("Invalid refresh token");
		}

		String tokenHash = tokenHasher.hash(refreshToken);
		Session session = sessionRepository.findActiveByRefreshTokenHash(tokenHash)
			.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

		if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Refresh token expired");
		}

		User user = session.getUser();
		List<String> roles = userRoleRepository.findActiveByUserId(user.getId()).stream()
			.map(ur -> ur.getRole().getName())
			.toList();

		String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
		return AuthResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(refreshToken)
			.accessTokenExpiresInMs(jwtService.getAccessTokenExpirationMs())
			.user(toUserResponse(user, roles))
			.build();
	}

	private AuthResponse buildAuthResponse(User user, List<String> roles) {
		String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
		String refreshToken = jwtService.generateRefreshToken(user.getId());

		Session session = new Session();
		session.setUser(user);
		session.setRefreshTokenHash(tokenHasher.hash(refreshToken));
		session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000));
		session.setCreatedBy(user.getId());
		session.setUpdatedBy(user.getId());
		sessionRepository.save(session);

		return AuthResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpiresInMs(jwtService.getAccessTokenExpirationMs())
			.user(toUserResponse(user, roles))
			.build();
	}

	private AuthResponse.UserResponse toUserResponse(User user, List<String> roles) {
		return AuthResponse.UserResponse.builder()
			.id(user.getId())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.email(user.getEmail())
			.organizationName(user.getOrganization().getName())
			.roles(roles)
			.build();
	}

}
