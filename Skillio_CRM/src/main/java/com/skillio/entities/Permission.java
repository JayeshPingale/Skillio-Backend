package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;
    
    private String permissionName; // CREATE_LEAD, READ_LEAD, UPDATE_LEAD, DELETE_LEAD, RECEIVE_PAYMENT, etc.
    private String module; // LEAD, ENROLLMENT, PAYMENT, BATCH, STUDENT, COMMISSION
    private String action; // CREATE, READ, UPDATE, DELETE
    private String description;
    
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermission> rolePermissions;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
