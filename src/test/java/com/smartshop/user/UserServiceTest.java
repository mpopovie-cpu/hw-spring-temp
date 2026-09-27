package com.smartshop.user;

import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createsUserSuccessfully() {
        when(userRepository.existsByUsername("mixa")).thenReturn(false);
        when(userRepository.existsByEmail("mixa@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.createUser("mixa", "mixa@example.com", "hash", Role.CUSTOMER);

        assertThat(user.getUsername()).isEqualTo("mixa");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("mixa")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("mixa", "mixa@example.com", "hash", Role.CUSTOMER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("mixa")).thenReturn(false);
        when(userRepository.existsByEmail("mixa@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("mixa", "mixa@example.com", "hash", Role.CUSTOMER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void throwsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}