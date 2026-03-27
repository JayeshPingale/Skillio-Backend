package com.skillio.services;

import com.skillio.dto.CreateUserRequest;
import com.skillio.dto.UpdateUserRequest;
import com.skillio.dto.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    UserResponse getUserById(Long userId);
    List<UserResponse> getAllUsers();
    void deleteUser(Long userId);
}
