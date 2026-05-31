package com.teamtasktracker.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

	private final Long id;

	private final String email;

	private final String passwordHash;

	private final Long organizationId;

	private final List<String> roles;

	private final List<String> permissions;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = roles.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
			.collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
		permissions.stream()
			.map(SimpleGrantedAuthority::new)
			.forEach(authorities::add);
		return authorities;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean hasPermission(String permission) {
		return permissions.contains(permission);
	}

	public boolean hasRole(String role) {
		return roles.contains(role);
	}

}
