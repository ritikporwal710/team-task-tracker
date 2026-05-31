package com.teamtasktracker.backend.dto.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {

	private List<T> data;

	private int page;

	private int limit;

	private long total;

	private int totalPages;

}
