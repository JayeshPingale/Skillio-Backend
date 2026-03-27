package com.skillio.controller;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.UserLoginRequest;
import com.skillio.dto.UserLoginResponse;
import com.skillio.entities.User;
import com.skillio.repositories.UserRepository;
import com.skillio.security.JwtTokenProvider;
import com.skillio.security.PermissionNameNormalizer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(value = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        //  Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        //  Generate JWT
        String token = jwtTokenProvider.generateToken(authentication);

        // Fetch user info
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getEmail()));

//res
        UserLoginResponse response = new UserLoginResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getRole().getRoleName(),
                user.getFullName(),
                user.getRole().getRolePermissions().stream()
                        .map(rolePermission -> rolePermission.getPermission())
                        .filter(Objects::nonNull)
                        .map(permission -> permission.getPermissionName())
                        .filter(Objects::nonNull)
                        .map(PermissionNameNormalizer::normalize)
                        .toList()
        );

        return ResponseEntity.ok(response);
    }
}
