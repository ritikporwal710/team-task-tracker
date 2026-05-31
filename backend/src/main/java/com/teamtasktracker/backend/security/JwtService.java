package com.teamtasktracker.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;

	private final long accessTokenExpirationMs;

	private final long refreshTokenExpirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
			@Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMs = accessTokenExpirationMs;
		this.refreshTokenExpirationMs = refreshTokenExpirationMs;
	}

	public String generateAccessToken(Long userId, String email, Collection<String> roles) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("email", email)
			.claim("roles", roles)
			.claim("type", "access")
			.issuedAt(new java.util.Date())
			.expiration(new java.util.Date(System.currentTimeMillis() + accessTokenExpirationMs))
			.signWith(secretKey)
			.compact();
	}

	public String generateRefreshToken(Long userId) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("type", "refresh")
			.issuedAt(new java.util.Date())
			.expiration(new java.util.Date(System.currentTimeMillis() + refreshTokenExpirationMs))
			.signWith(secretKey)
			.compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean isAccessToken(Claims claims) {
		return "access".equals(claims.get("type", String.class));
	}

	public boolean isRefreshToken(Claims claims) {
		return "refresh".equals(claims.get("type", String.class));
	}

	@SuppressWarnings("unchecked")
	public List<String> getRoles(Claims claims) {
		Object roles = claims.get("roles");
		if (roles instanceof List<?> list) {
			return list.stream().map(String::valueOf).toList();
		}
		return List.of();
	}

	public long getAccessTokenExpirationMs() {
		return accessTokenExpirationMs;
	}

	public long getRefreshTokenExpirationMs() {
		return refreshTokenExpirationMs;
	}

}
