package com.skillio.repositories;

import com.skillio.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    @Query("SELECT p.permissionName FROM RolePermission rp " +
           "JOIN rp.permission p " +
           "WHERE rp.role.roleId = :roleId")
    Set<String> findPermissionNamesByRoleId(@Param("roleId") Long roleId);
    
    List<RolePermission> findByRoleRoleId(Long roleId);
    
    void deleteByRoleRoleId(Long roleId);
}
