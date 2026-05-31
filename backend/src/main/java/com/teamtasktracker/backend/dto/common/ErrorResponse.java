package com.teamtasktracker.backend.dto.common;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

	private int status;

	private String message;

	private Instant timestamp;

}
