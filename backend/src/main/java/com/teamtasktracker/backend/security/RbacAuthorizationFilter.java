package com.teamtasktracker.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtasktracker.backend.dto.common.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RbacAuthorizationFilter extends OncePerRequestFilter {

	private static final Map<String, String> ROUTE_PERMISSIONS = Map.ofEntries(
			Map.entry("POST:/api/projects", "PROJECT_CREATE"),
			Map.entry("PUT:/api/projects/*", "PROJECT_UPDATE"),
			Map.entry("PATCH:/api/projects/*", "PROJECT_UPDATE"),
			Map.entry("DELETE:/api/projects/*", "PROJECT_DELETE"),
			Map.entry("POST:/api/tasks", "TASK_CREATE"),
			Map.entry("PUT:/api/tasks/*", "TASK_UPDATE"),
			Map.entry("PATCH:/api/tasks/*/assign", "TASK_ASSIGN"),
			Map.entry("PATCH:/api/tasks/*/status", "TASK_STATUS_UPDATE"),
			Map.entry("DELETE:/api/tasks/*", "TASK_DELETE"),
			Map.entry("POST:/api/users", "USER_CREATE"),
			Map.entry("PUT:/api/users/*", "USER_UPDATE"),
			Map.entry("PATCH:/api/users/*", "USER_UPDATE"),
			Map.entry("DELETE:/api/users/*", "USER_DELETE"));

	private final ObjectMapper objectMapper;

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/health")
				|| path.startsWith("/api/auth")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			filterChain.doFilter(request, response);
			return;
		}

		Optional<String> requiredPermission = resolveRequiredPermission(request);
		if (requiredPermission.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!principal.getPermissions().contains(requiredPermission.get())) {
			writeForbidden(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private Optional<String> resolveRequiredPermission(HttpServletRequest request) {
		String method = request.getMethod();
		String path = request.getRequestURI().replaceAll("/+$", "");

		// Most specific routes first
		String[] orderedPatterns = {
				"PATCH:/api/tasks/*/assign",
				"PATCH:/api/tasks/*/status",
				"POST:/api/projects",
				"PUT:/api/projects/*",
				"PATCH:/api/projects/*",
				"DELETE:/api/projects/*",
				"POST:/api/tasks",
				"PUT:/api/tasks/*",
				"PATCH:/api/tasks/*",
				"DELETE:/api/tasks/*",
				"POST:/api/users",
				"PUT:/api/users/*",
				"PATCH:/api/users/*",
				"DELETE:/api/users/*"
		};

		for (String pattern : orderedPatterns) {
			int colon = pattern.indexOf(':');
			String patternMethod = pattern.substring(0, colon);
			String patternPath = pattern.substring(colon + 1);
			if (patternMethod.equals(method) && matchesPattern(path, patternPath)) {
				return Optional.of(ROUTE_PERMISSIONS.get(pattern));
			}
		}
		return Optional.empty();
	}

	private boolean matchesPattern(String path, String pattern) {
		if (pattern.equals(path)) {
			return true;
		}
		if (!pattern.contains("*")) {
			return false;
		}
		String prefix = pattern.substring(0, pattern.indexOf('*'));
		String suffix = pattern.substring(pattern.indexOf('*') + 1);
		return path.startsWith(prefix) && path.endsWith(suffix) && path.length() >= prefix.length() + suffix.length();
	}

	private void writeForbidden(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ErrorResponse body = ErrorResponse.builder()
			.status(403)
			.code("FORBIDDEN")
			.message("You do not have permission to perform this action")
			.build();
		objectMapper.writeValue(response.getOutputStream(), body);
	}

}
