package com.skillio.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final List<String> DEFAULT_PERMISSIONS = List.of(
            "AUDIT_LOG_LIST",
            "AUDIT_LOG_READ",
            "BATCH_CREATE",
            "BATCH_DELETE",
            "BATCH_LIST",
            "BATCH_READ",
            "BATCH_UPDATE",
            "COMMISSION_CREATE",
            "COMMISSION_DELETE",
            "COMMISSION_LIST",
            "COMMISSION_PAYMENT_CREATE",
            "COMMISSION_PAYMENT_DELETE",
            "COMMISSION_PAYMENT_LIST",
            "COMMISSION_PAYMENT_READ",
            "COMMISSION_PAYMENT_UPDATE",
            "COMMISSION_READ",
            "COMMISSION_UPDATE",
            "COURSE_CREATE",
            "COURSE_DELETE",
            "COURSE_LIST",
            "COURSE_READ",
            "COURSE_UPDATE",
            "ENROLLMENT_CREATE",
            "ENROLLMENT_DELETE",
            "ENROLLMENT_LIST",
            "ENROLLMENT_READ",
            "ENROLLMENT_UPDATE",
            "FOLLOW_UP_CREATE",
            "FOLLOW_UP_DELETE",
            "FOLLOW_UP_LIST",
            "FOLLOW_UP_READ",
            "FOLLOW_UP_UPDATE",
            "INVOICE_CREATE",
            "INVOICE_DELETE",
            "INVOICE_LIST",
            "INVOICE_READ",
            "INVOICE_UPDATE",
            "LEAD_CREATE",
            "LEAD_DELETE",
            "LEAD_LIST",
            "LEAD_READ",
            "LEAD_SOURCE_CREATE",
            "LEAD_SOURCE_DELETE",
            "LEAD_SOURCE_LIST",
            "LEAD_SOURCE_READ",
            "LEAD_SOURCE_UPDATE",
            "LEAD_STATUS_HISTORY_LIST",
            "LEAD_UPDATE",
            "NOTIFICATION_DELETE",
            "NOTIFICATION_LIST",
            "NOTIFICATION_UPDATE",
            "PAYMENT_CREATE",
            "PAYMENT_DELETE",
            "PAYMENT_INSTALLMENT_CREATE",
            "PAYMENT_INSTALLMENT_DELETE",
            "PAYMENT_INSTALLMENT_LIST",
            "PAYMENT_INSTALLMENT_READ",
            "PAYMENT_INSTALLMENT_UPDATE",
            "PAYMENT_LIST",
            "PAYMENT_READ",
            "PAYMENT_UPDATE",
            "PERMISSION_CREATE",
            "PERMISSION_DELETE",
            "PERMISSION_LIST",
            "PERMISSION_READ",
            "PERMISSION_UPDATE",
            "ROLE_CREATE",
            "ROLE_DELETE",
            "ROLE_LIST",
            "ROLE_READ",
            "ROLE_UPDATE",
            "STUDENT_CREATE",
            "STUDENT_DELETE",
            "STUDENT_LIST",
            "STUDENT_READ",
            "STUDENT_UPDATE",
            "TARGET_CREATE",
            "TARGET_DELETE",
            "TARGET_LIST",
            "TARGET_READ",
            "TARGET_UPDATE",
            "USER_CREATE",
            "USER_DELETE",
            "USER_LIST",
            "USER_READ",
            "USER_UPDATE"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePermissions();

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

    private void initializePermissions() {
        List<Permission> missingPermissions = DEFAULT_PERMISSIONS.stream()
                .filter(permissionName -> permissionRepository.findByPermissionName(permissionName).isEmpty())
                .map(this::buildPermission)
                .toList();

        if (missingPermissions.isEmpty()) {
            System.out.println("All permissions already initialized.");
            return;
        }

        permissionRepository.saveAll(missingPermissions);
        System.out.println("Initialized " + missingPermissions.size() + " permissions.");
    }

    private void assignAllPermissionsToAdminRole(Role adminRole) {
        Set<Long> assignedPermissionIds = rolePermissionRepository.findByRoleRoleId(adminRole.getRoleId()).stream()
                .map(rolePermission -> rolePermission.getPermission().getPermissionId())
                .collect(Collectors.toSet());

        List<Permission> missingPermissions = permissionRepository.findAll().stream()
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

    private Permission buildPermission(String permissionName) {
        Permission permission = new Permission();
        permission.setPermissionName(permissionName);
        permission.setModule(extractModule(permissionName));
        permission.setAction(extractAction(permissionName));
        permission.setDescription(permissionName + " permission");
        return permission;
    }

    private String extractModule(String permissionName) {
        int lastUnderscoreIndex = permissionName.lastIndexOf('_');
        if (lastUnderscoreIndex <= 0) {
            return permissionName;
        }
        return permissionName.substring(0, lastUnderscoreIndex);
    }

    private String extractAction(String permissionName) {
        int lastUnderscoreIndex = permissionName.lastIndexOf('_');
        if (lastUnderscoreIndex == -1 || lastUnderscoreIndex == permissionName.length() - 1) {
            return "ACCESS";
        }
        return permissionName.substring(lastUnderscoreIndex + 1);
    }
}
