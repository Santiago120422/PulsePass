package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;

import org.springframework.security.access.prepost.PreAuthorize;

public interface UserService {

    UserResponse register(RegisterUserRequest request);

    @PreAuthorize("@authz.canAccessEmail(#p0, authentication)")
    UserResponse findByEmail(String email);

    @PreAuthorize("@authz.canAccessUsername(#p0, authentication)")
    UserResponse findByUsername(String username);
}
