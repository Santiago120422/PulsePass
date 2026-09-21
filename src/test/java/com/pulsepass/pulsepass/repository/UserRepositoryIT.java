package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User newUser(String username, String email) {
        return User.builder().username(username).email(email).active(true).build();
    }

    // FR-USR-001
    @Test
    void shouldPersistAndRetrieveUser() {
        User saved = userRepository.save(newUser("juanp", "juanp@mail.com"));

        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

    // FR-USR-002 / BR-009
    @Test
    void shouldRejectDuplicateUsernameOrEmail() {
        userRepository.saveAndFlush(newUser("mariaf", "mariaf@mail.com"));

        User duplicateUsername = newUser("mariaf", "otro@mail.com");
        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(duplicateUsername));
    }

    // FR-USR-003 / FR-USR-004 / QT-004 (User 1:1 UserProfile)
    @Test
    void shouldPersistOneProfilePerUserAndRetrieveData() {
        User user = userRepository.save(newUser("carlosr", "carlosr@mail.com"));

        UserProfile profile = UserProfile.builder()
                .firstName("Carlos")
                .lastName("Ramírez")
                .phone("3001234567")
                .city("Barranquilla")
                .birthDate(LocalDate.of(1995, 4, 12))
                .user(user)
                .build();
        user.setProfile(profile);

        userRepository.saveAndFlush(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getProfile()).isNotNull();
        assertThat(found.getProfile().getCity()).isEqualTo("Barranquilla");
    }

    // BR-004 (máximo un perfil por usuario -> FK UNIQUE en user_profiles.user_id)
    @Test
    void shouldRejectSecondProfileForSameUser() {
        User user = userRepository.saveAndFlush(newUser("anap", "anap@mail.com"));

        UserProfile profile1 = UserProfile.builder()
                .firstName("Ana").lastName("Pérez").user(user).build();
        user.setProfile(profile1);
        userRepository.saveAndFlush(user);

        // Segundo perfil apuntando al mismo usuario, insertado directo con
        // EntityManager (no vía user.setProfile, porque el mapeo 1:1 en User
        // ya está ocupado por profile1 en memoria)
        UserProfile profile2 = UserProfile.builder()
                .firstName("Ana2").lastName("Pérez2").user(user).build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            entityManager.persist(profile2);
            entityManager.flush();
        });
    }
}