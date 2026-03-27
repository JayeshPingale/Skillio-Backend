package com.skillio.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.skillio.entities.RolePermission;
import com.skillio.entities.User;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
@Data
public class CustomUserDetails implements UserDetails {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;
        String roleAuthority = roleName == null ? null
                : roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        List<GrantedAuthority> permissionAuthorities = user.getRole().getRolePermissions().stream()
                .map(RolePermission::getPermission)
                .filter(Objects::nonNull)
                .map(permission -> permission.getPermissionName())
                .filter(Objects::nonNull)
                .map(PermissionNameNormalizer::normalize)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        if (roleAuthority == null) {
            return permissionAuthorities;
        }

        return java.util.stream.Stream.concat(
                java.util.stream.Stream.of(new SimpleGrantedAuthority(roleAuthority)),
                permissionAuthorities.stream())
                .toList();
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getIsActive(); }
}
