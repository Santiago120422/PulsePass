package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.*;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, userMapper, CLOCK);
    }

    private static RegisterUserRequest request(LocalDate birthDate) {
        return new RegisterUserRequest("andrea", "Andrea@Email.com", "Andrea", "Perez",
                "3001234567", "Santa Marta", birthDate);
    }

    private static UserResponse response() {
        return new UserResponse(1L, "andrea", "andrea@email.com", true, "Andrea", "Perez",
                "3001234567", "Santa Marta", LocalDate.of(2001, 1, 1));
    }

    @Test // TEST-USER-001 + BR-USER-003 + BR-USER-004
    void register_validUser_createsActiveUserWithProfile() {
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(response());

        UserResponse result = service.register(request(LocalDate.of(2001, 1, 1)));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getEmail()).isEqualTo("andrea@email.com");
        assertThat(saved.getProfile()).isNotNull();
        assertThat(saved.getProfile().getUser()).isSameAs(saved);
        assertThat(saved.getProfile().getBirthDate()).isEqualTo(LocalDate.of(2001, 1, 1));
        assertThat(saved.getProfile().getFirstName()).isEqualTo("Andrea");
        assertThat(result.username()).isEqualTo("andrea");
    }

    @Test // TEST-USER-002
    void register_duplicateUsername_throwsDuplicateResource() {
        when(userRepository.existsByUsername("andrea")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request(LocalDate.of(2001, 1, 1))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test // TEST-USER-003 + BR-USER-002
    void register_duplicateEmailIgnoringCase_throwsDuplicateResource() {
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(eq("andrea@email.com"))).thenReturn(true);

        assertThatThrownBy(() -> service.register(request(LocalDate.of(2001, 1, 1))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test // TEST-USER-004
    void register_futureBirthDate_throwsBusinessRule() {
        assertThatThrownBy(() -> service.register(request(NOW.toLocalDate().plusDays(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmail_existing_returnsDto() {
        User user = user("andrea@email.com", true, LocalDate.of(2001, 1, 1));
        when(userRepository.findByEmailIgnoreCase("ANDREA@email.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response());

        assertThat(service.findByEmail("ANDREA@email.com").email()).isEqualTo("andrea@email.com");
    }

    @Test
    void findByEmail_missing_throwsNotFound() {
        when(userRepository.findByEmailIgnoreCase("x@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("x@email.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUsername_existing_returnsDto() {
        User user = user("andrea@email.com", true, LocalDate.of(2001, 1, 1));
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response());

        assertThat(service.findByUsername("andrea").username()).isEqualTo("andrea");
    }

    @Test
    void findByUsername_missing_throwsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
