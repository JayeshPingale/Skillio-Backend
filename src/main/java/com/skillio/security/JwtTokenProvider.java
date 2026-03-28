package com.skillio.security;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.skillio.entities.User;
import com.skillio.repositories.UserRepository;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserRepository userRepository;

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationMillis;

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().getRoleName()); // single role
        claims.put("fullName", user.getFullName());
        claims.put("permissions", user.getRole().getRolePermissions().stream()
                .map(rolePermission -> rolePermission.getPermission())
                .filter(Objects::nonNull)
                .map(permission -> permission.getPermissionName())
                .filter(Objects::nonNull)
                .map(PermissionNameNormalizer::normalize)
                .toList());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token).getBody();
    }

    public List<String> getPermissions(String token) {
        Object permissionsClaim = getClaims(token).get("permissions");
        if (permissionsClaim instanceof List<?> permissionList) {
            return permissionList.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .toList();
        }
        return Collections.emptyList();
    }

    public String getRole(String token) {
        Object roleClaim = getClaims(token).get("role");
        return roleClaim == null ? null : String.valueOf(roleClaim);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new RuntimeException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new RuntimeException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new RuntimeException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("JWT claims string is empty.");
        }
    }
}
