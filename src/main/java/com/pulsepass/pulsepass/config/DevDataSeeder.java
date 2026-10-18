package com.pulsepass.pulsepass.config;

import com.pulsepass.pulsepass.domain.UserRole;
import com.pulsepass.pulsepass.domain.UserRoleId;
import com.pulsepass.pulsepass.domain.enums.Role;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.repository.UserRoleRepository;
import com.pulsepass.pulsepass.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Semilla LOCAL controlada (perfil "dev"): cuentas de demostracion con rol privilegiado.
 * Es la unica via para tener ADMIN/STAFF: el registro publico solo crea USER (NFR-SEC009).
 */
@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final String seedPassword;

    public DevDataSeeder(AuthService authService, UserRepository userRepository, UserRoleRepository roleRepository,
                         @Value("${pulsepass.seed.password}") String seedPassword) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("admin", "Admin", Role.ADMIN);
        seed("laura", "Laura", Role.STAFF);
        seed("miguel", "Miguel", Role.STAFF);
    }

    private void seed(String username, String firstName, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        var created = authService.register(new RegisterUserRequest(
                username, username + "@pulsepass.local", firstName, "Demo", null, "Santa Marta",
                LocalDate.of(1990, 1, 1), seedPassword));
        roleRepository.deleteById(new UserRoleId(created.id(), Role.USER));
        roleRepository.save(new UserRole(new UserRoleId(created.id(), role)));
    }
}
