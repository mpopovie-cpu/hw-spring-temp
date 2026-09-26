package com.smartshop.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsByUsername() {
        User user = new User("mixa", "mixa@example.com", "hash", Role.CUSTOMER, true, LocalDateTime.now());
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("mixa");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("mixa@example.com");
    }

    @Test
    void findsByEmail() {
        User user = new User("anna", "anna@example.com", "hash", Role.MANAGER, true, LocalDateTime.now());
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("anna@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("anna");
    }

    @Test
    void existsByUsernameReflectsPersistedState() {
        assertThat(userRepository.existsByUsername("ghost")).isFalse();

        userRepository.save(new User("ghost", "ghost@example.com", "hash", Role.CUSTOMER, true, LocalDateTime.now()));

        assertThat(userRepository.existsByUsername("ghost")).isTrue();
    }
}