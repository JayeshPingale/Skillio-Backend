package com.skillio.security;

import java.util.Set;

public final class PermissionNameNormalizer {

    private static final Set<String> ACTION_PREFIXES = Set.of("CREATE", "READ", "UPDATE", "DELETE", "LIST");

    private PermissionNameNormalizer() {
    }

    public static String normalize(String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            return permissionName;
        }

        String trimmed = permissionName.trim().toUpperCase();
        String[] parts = trimmed.split("_");

        if (parts.length >= 2 && ACTION_PREFIXES.contains(parts[0])) {
            return String.join("_", java.util.Arrays.copyOfRange(parts, 1, parts.length)) + "_" + parts[0];
        }

        return trimmed;
    }

    public static String toLegacy(String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            return permissionName;
        }

        String trimmed = permissionName.trim().toUpperCase();
        String[] parts = trimmed.split("_");

        if (parts.length >= 2 && ACTION_PREFIXES.contains(parts[parts.length - 1])) {
            return parts[parts.length - 1] + "_" + String.join("_", java.util.Arrays.copyOf(parts, parts.length - 1));
        }

        return trimmed;
    }
}
