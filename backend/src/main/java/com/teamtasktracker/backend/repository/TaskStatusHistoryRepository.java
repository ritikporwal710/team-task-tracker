package com.teamtasktracker.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamtasktracker.backend.domain.entity.TaskStatusHistory;

public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {
}
