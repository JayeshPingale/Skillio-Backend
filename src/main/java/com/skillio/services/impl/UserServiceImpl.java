package com.skillio.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.dto.AuditUserDTO;
import com.skillio.dto.CreateUserRequest;
import com.skillio.dto.UpdateUserRequest;
import com.skillio.dto.UserResponse;
import com.skillio.entities.Role;
import com.skillio.entities.User;
import com.skillio.exepection.DuplicateEmailException;
import com.skillio.exepection.DuplicatePhoneException;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.RoleRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        if (userRepository.findBycontactNumber(request.getContactNumber()).isPresent()) {
            throw new DuplicatePhoneException("Contact already exists: " + request.getContactNumber());
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setContactNumber(request.getContactNumber());
        user.setRole(role);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // ✅ Convert to audit DTO
        AuditUserDTO auditDTO = convertToAuditDTO(savedUser);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("User", savedUser.getUserId(), "CREATE", 
                                      null, auditDTO, performedBy);

        log.info("User created successfully with ID: {}", savedUser.getUserId());

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // ✅ Convert old user to audit DTO
        AuditUserDTO oldUserDTO = convertToAuditDTO(user);

        if (!user.getContactNumber().equals(request.getContactNumber())) {
            if (userRepository.findBycontactNumber(request.getContactNumber()).isPresent()) {
                throw new DuplicatePhoneException("Contact already exists: " + request.getContactNumber());
            }
        }

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateEmailException("Email already taken: " + request.getEmail());
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setProfilePic(request.getProfilePic());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));
            user.setRole(role);
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);

        // ✅ Convert updated user to audit DTO
        AuditUserDTO newUserDTO = convertToAuditDTO(updatedUser);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("User", userId, "UPDATE", 
                                      oldUserDTO, newUserDTO, performedBy);

        log.info("User updated successfully with ID: {}", userId);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // ✅ Convert to audit DTO before deletion
        AuditUserDTO auditDTO = convertToAuditDTO(user);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("User", userId, "DELETE", 
                                      auditDTO, null, performedBy);

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * ✅ Convert User entity to audit DTO (no circular references)
     */
    private AuditUserDTO convertToAuditDTO(User user) {
        AuditUserDTO dto = new AuditUserDTO();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setContactNumber(user.getContactNumber());
        dto.setProfilePic(user.getProfilePic());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setIsActive(user.getIsActive());
        return dto;
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setContactNumber(user.getContactNumber());
        response.setProfilePic(user.getProfilePic());

        response.setRoleName(
                Optional.ofNullable(user.getRole())
                        .map(Role::getRoleName)
                        .orElse(null)
        );

        response.setIsActive(user.getIsActive());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    private User getLoggedInUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch logged-in user from SecurityContext", e);
        }
        return null;
    }
    
    // ❌ REMOVED cloneUser() method  not needed anymore
}
