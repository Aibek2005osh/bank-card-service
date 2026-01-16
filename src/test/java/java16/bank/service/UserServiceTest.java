package java16.bank.service;

import java16.bank.entity.User;
import java16.bank.enums.Role;
import java16.bank.repository.UserRepo;
import java16.bank.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_pass");
        user.setFullName("Test User");
        user.setEmail("test@mail.com");
        user.setRole(Role.USER);
        user.setEnabled(true);
    }

    // ---------------- loadUserByUsername ----------------

    @Test
    void loadUserByUsername_success() {
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_notFound_throwException() {
        when(userRepo.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown"));
    }

    // ---------------- createUser ----------------

    @Test
    void createUser_success() {
        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_pass");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.createUser(
                "testuser",
                "password",
                "Test User",
                "test@mail.com",
                Role.USER
        );

        assertNotNull(saved);
        assertEquals("testuser", saved.getUsername());
        assertEquals("encoded_pass", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(saved.getEnabled());

        verify(passwordEncoder).encode("password");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void createUser_usernameExists_throwException() {
        when(userRepo.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.createUser(
                        "testuser", "password",
                        "Test User", "test@mail.com", Role.USER
                ));

        verify(userRepo, never()).save(any());
    }

    @Test
    void createUser_emailExists_throwException() {
        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.createUser(
                        "testuser", "password",
                        "Test User", "test@mail.com", Role.USER
                ));

        verify(userRepo, never()).save(any());
    }

    // ---------------- findById ----------------

    @Test
    void findById_success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.findById(1L);

        assertEquals(1L, found.getId());
    }

    @Test
    void findById_notFound_throwException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.findById(99L));
    }

    // ---------------- findByUsername ----------------

    @Test
    void findByUsername_success() {
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        User found = userService.findByUsername("testuser");

        assertEquals("testuser", found.getUsername());
    }

    @Test
    void findByUsername_notFound_throwException() {
        when(userRepo.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.findByUsername("unknown"));
    }

    // ---------------- getAllUsers ----------------

    @Test
    void getAllUsers_success() {
        when(userRepo.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
    }

    // ---------------- deleteUser ----------------

    @Test
    void deleteUser_success() {
        doNothing().when(userRepo).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepo).deleteById(1L);
    }

    // ---------------- toggleUserStatus ----------------

    @Test
    void toggleUserStatus_success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Boolean oldStatus = user.getEnabled();

        User updated = userService.toggleUserStatus(1L);

        assertEquals(!oldStatus, updated.getEnabled());
        verify(userRepo).save(user);
    }

    @Test
    void toggleUserStatus_userNotFound_throwException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.toggleUserStatus(99L));
    }
}
