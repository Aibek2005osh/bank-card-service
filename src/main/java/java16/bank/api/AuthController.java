package java16.bank.api;
import java16.bank.dto.response.ApiResponse;
import java16.bank.dto.response.AuthResponse;
import java16.bank.dto.LoginRequest;
import java16.bank.dto.UserRequest;
import java16.bank.dto.response.UserResponse;
import java16.bank.entity.User;
import java16.bank.mapper.UserMapper;
import java16.bank.service.impl.UserServiceImpl;
import java16.bank.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserServiceImpl userService;

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        User user = userService.findByUsername(request.getUsername());
        UserResponse userResponse = userMapper.toResponse(user);

        AuthResponse authResponse = new AuthResponse(token, userResponse);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRequest request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getEmail(),
                request.getRole()
        );

        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }
}