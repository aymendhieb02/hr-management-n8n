package com.xtensus.hrmanagementapi.security.jwt;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;
    public JwtService(@Value("${app.security.jwt-secret}") String jwtSecret, @Value("${app.security.jwt-expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }
    public String generateToken(Employe employe) {
        Date now = new Date(); Date expiresAt = new Date(now.getTime() + expirationMs); RoleType role = RoleType.fromDatabaseRole(employe.getRole());
        return Jwts.builder().subject(employe.getUsername()).claim("userId", employe.getId()).claim("username", employe.getUsername()).claim("role", role.name()).issuedAt(now).expiration(expiresAt).signWith(signingKey).compact();
    }
    public String extractUsername(String token) { return claims(token).getSubject(); }
    public Long extractUserId(String token) { Object userId = claims(token).get("userId"); return userId instanceof Number number ? number.longValue() : Long.valueOf(userId.toString()); }
    public String extractRole(String token) { return claims(token).get("role", String.class); }
    public Date extractExpiration(String token) { return claims(token).getExpiration(); }
    public boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }
    public boolean validateToken(String token, UserDetails userDetails) { try { return extractUsername(token).equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token); } catch (JwtException | IllegalArgumentException exception) { return false; } }
    public long getExpirationMs() { return expirationMs; }
    private Claims claims(String token) { return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload(); }
}
