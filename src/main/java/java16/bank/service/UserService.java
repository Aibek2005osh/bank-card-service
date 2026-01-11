package java16.bank.service;



import java16.bank.entity.User;
import java16.bank.enums.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User createUser(String username, String password, String fullName, String email, Role role);

    User findById(Long id);

    User findByUsername(String username);

    List<User> getAllUsers();

    void deleteUser(Long userId);

    User toggleUserStatus(Long userId);
}
