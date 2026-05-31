package com.teamtasktracker.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtasktracker.backend.dto.user.UserSummaryResponse;
import com.teamtasktracker.backend.repository.UserRepository;
import com.teamtasktracker.backend.repository.UserRoleRepository;
import com.teamtasktracker.backend.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	private final UserRoleRepository userRoleRepository;

	private final SecurityUtils securityUtils;

	@Transactional(readOnly = true)
	public List<UserSummaryResponse> listOrganizationMembers() {
		var organizationId = securityUtils.currentUser().getOrganizationId();
		return userRepository.findActiveByOrganizationId(organizationId).stream()
			.map(user -> {
				var roles = userRoleRepository.findActiveByUserId(user.getId()).stream()
					.map(ur -> ur.getRole().getName())
					.toList();
				return UserSummaryResponse.builder()
					.id(user.getId())
					.firstName(user.getFirstName())
					.lastName(user.getLastName())
					.email(user.getEmail())
					.roles(roles)
					.build();
			})
			.toList();
	}

}
