package com.skillio.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.entities.Permission;
import com.skillio.entities.Role;
import com.skillio.entities.RolePermission;
import com.skillio.entities.User;
import com.skillio.repositories.PermissionRepository;
import com.skillio.repositories.RolePermissionRepository;
import com.skillio.repositories.RoleRepository;
import com.skillio.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        String adminEmail = "admin@skillio.com";
        String adminPassword = "admin@123";

        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("ROLE_ADMIN");
                    newRole.setDescription("System Administrator");
                    newRole.setIsActive(true);
                    return roleRepository.save(newRole);
                });

        assignAllPermissionsToAdminRole(adminRole);

        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            
            // Step 1: Admin user create karo
            User adminUser = new User();
            adminUser.setFullName("Admin User");
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setContactNumber("9999999999");
            adminUser.setRole(adminRole);
            adminUser.setIsActive(true);

            // Step 2: Save karo DB mein
            userRepository.save(adminUser);

            System.out.println("======================================================");
            System.out.println("Default Admin user created successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Password: " + adminPassword + " (Use BCrypt to encode)");
            System.out.println("======================================================");
        } else {
            System.out.println("Admin user already exists. Skipping creation.");
        }
    }

    private void assignAllPermissionsToAdminRole(Role adminRole) {
        java.util.Set<Long> assignedPermissionIds = rolePermissionRepository.findByRoleRoleId(adminRole.getRoleId()).stream()
                .map(rolePermission -> rolePermission.getPermission().getPermissionId())
                .collect(java.util.stream.Collectors.toSet());

        java.util.List<Permission> missingPermissions = permissionRepository.findAll().stream()
                .filter(permission -> !assignedPermissionIds.contains(permission.getPermissionId()))
                .toList();

        if (missingPermissions.isEmpty()) {
            return;
        }

        missingPermissions.forEach(permission -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(adminRole);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        });

        System.out.println("Assigned " + missingPermissions.size() + " permissions to ROLE_ADMIN.");
    }
}
