package com.teamtasktracker.backend.dto.auth;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

	private String accessToken;

	private String refreshToken;

	private long accessTokenExpiresInMs;

	private UserResponse user;

	@Getter
	@Builder
	public static class UserResponse {

		private Long id;

		private String firstName;

		private String lastName;

		private String email;

		private String organizationName;

		private List<String> roles;

	}

}
