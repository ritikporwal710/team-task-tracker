package com.teamtasktracker.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamtasktracker.backend.dto.user.UserSummaryResponse;
import com.teamtasktracker.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<List<UserSummaryResponse>> listMembers() {
		return ResponseEntity.ok(userService.listOrganizationMembers());
	}

}
