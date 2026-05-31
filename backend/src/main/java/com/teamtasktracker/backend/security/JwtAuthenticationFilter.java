package com.teamtasktracker.backend.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.teamtasktracker.backend.repository.UserRepository;
import com.teamtasktracker.backend.repository.UserRoleRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final UserRepository userRepository;

	private final UserRoleRepository userRoleRepository;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);
		try {
			Claims claims = jwtService.parseToken(token);
			if (!jwtService.isAccessToken(claims)) {
				filterChain.doFilter(request, response);
				return;
			}

			Long userId = Long.parseLong(claims.getSubject());
			if (SecurityContextHolder.getContext().getAuthentication() != null) {
				filterChain.doFilter(request, response);
				return;
			}

			var user = userRepository.findById(userId)
				.filter(u -> u.isActive())
				.orElse(null);
			if (user == null) {
				filterChain.doFilter(request, response);
				return;
			}

			var roleNames = userRoleRepository.findActiveByUserId(userId).stream()
				.map(ur -> ur.getRole().getName())
				.toList();

			UserPrincipal principal = new UserPrincipal(
					user.getId(),
					user.getEmail(),
					user.getPasswordHash(),
					user.getOrganization().getId(),
					roleNames);

			var authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					principal.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (Exception ignored) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

}
