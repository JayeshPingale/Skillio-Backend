package com.skillio.repositories;

import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleRoleId(Long roleId);
    List<User> findByRoleRoleName(String roleName);
    Optional<User> findBycontactNumber(String contactNumber);
    List<User> findByIsActive(Boolean isActive);
//    List<User> findByIsContactNumber(Boolean isActive);
    
}
