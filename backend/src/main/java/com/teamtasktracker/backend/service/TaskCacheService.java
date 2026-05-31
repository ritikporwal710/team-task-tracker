package com.teamtasktracker.backend.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class TaskCacheService {

	public static final String TASK_LIST_CACHE = "taskListByAssignee";

	@CacheEvict(value = TASK_LIST_CACHE, allEntries = true)
	public void invalidateAllTaskLists() {
		// Annotation-driven cache eviction on task mutations
	}

}
