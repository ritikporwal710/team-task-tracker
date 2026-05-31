package com.teamtasktracker.backend.dto.auth;

import com.teamtasktracker.backend.domain.enums.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

	@NotBlank
	@Size(max = 100)
	private String firstName;

	@Size(max = 100)
	private String lastName;

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 8, max = 100)
	private String password;

	@NotNull
	private RoleName role;

}
