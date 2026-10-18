package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.domain.UserRole;
import com.pulsepass.pulsepass.domain.UserRoleId;
import com.pulsepass.pulsepass.domain.enums.Role;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Orquesta registro (User + hash + ROLE_USER) en UNA transaccion. No reemplaza a UserService. */
@Service
public class AuthService {

    private final UserService userService;
    private final UserCredentialRepository credentialRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AuthService(UserService userService,
                       UserCredentialRepository credentialRepository,
                       UserRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       Clock clock) {
        this.userService = userService;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /** FR-SECAUTH-001. El rol inicial es SIEMPRE USER: nunca se toma del cliente. */
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        UserResponse created = userService.register(request);   // unicidad y reglas de negocio existentes
        credentialRepository.save(new UserCredential(
                created.id(), passwordEncoder.encode(request.password()), LocalDateTime.now(clock)));
        roleRepository.save(new UserRole(new UserRoleId(created.id(), Role.USER)));
        return created;
    }
}
