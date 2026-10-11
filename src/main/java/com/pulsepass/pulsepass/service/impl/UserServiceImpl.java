package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import static com.pulsepass.pulsepass.service.impl.Validations.*;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Clock clock;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, Clock clock) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.clock = clock;
    }

    /** BR-USER-004: User + UserProfile se persisten en la misma transacción (cascade ALL). */
    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        requireRequest(request);
        String username = requireText(request.username(), "username");
        // El email se normaliza a minúsculas: la columna es UNIQUE sensible a mayúsculas en PostgreSQL.
        String email = requireText(request.email(), "email").toLowerCase(Locale.ROOT);

        // BR-USER-005
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("Birth date cannot be in the future.");
        }
        // BR-USER-001
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists.");
        }
        // BR-USER-002
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already exists.");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .active(true) // BR-USER-003
                .build();

        UserProfile profile = UserProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .city(request.city())
                .birthDate(request.birthDate())
                .user(user)
                .build();
        user.setProfile(profile);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
    }

    @Override
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("User", username));
    }
}
