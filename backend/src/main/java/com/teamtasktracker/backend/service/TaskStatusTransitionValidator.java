package com.teamtasktracker.backend.service;

import org.springframework.stereotype.Service;

import com.teamtasktracker.backend.domain.enums.TaskStatus;
import com.teamtasktracker.backend.exception.ApiException;

@Service
public class TaskStatusTransitionValidator {

	public void validate(TaskStatus from, TaskStatus to) {
		if (from == to) {
			return;
		}
		if (to == TaskStatus.BLOCKED) {
			if (from == TaskStatus.DONE) {
				throw new ApiException(400, "INVALID_STATUS_TRANSITION",
						"Cannot block a completed task");
			}
			return;
		}
		if (from == TaskStatus.BLOCKED) {
			if (to != TaskStatus.TODO) {
				throw new ApiException(400, "INVALID_STATUS_TRANSITION",
						"Unblock the task by moving it back to TODO first");
			}
			return;
		}
		boolean valid = switch (from) {
			case TODO -> to == TaskStatus.IN_PROGRESS;
			case IN_PROGRESS -> to == TaskStatus.IN_REVIEW;
			case IN_REVIEW -> to == TaskStatus.DONE;
			case DONE, BLOCKED -> false;
		};
		if (!valid) {
			throw new ApiException(400, "INVALID_STATUS_TRANSITION",
					"Invalid transition from " + from + " to " + to
							+ ". Allowed: TODO→IN_PROGRESS→IN_REVIEW→DONE, or BLOCKED from any active state");
		}
	}

}
