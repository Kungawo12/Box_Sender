package com.boxsender.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                         EmployeeRepository employeeRepo,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            Employee employee = employeeRepo.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "firstName", employee.getFirstName(),
                "email", employee.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate input
            if (request.firstName() == null || request.firstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "First name is required"));
            }
            if (request.lastName() == null || request.lastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Last name is required"));
            }
            if (request.email() == null || request.email().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (request.password() == null || request.password().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            }

            // Check if email already exists
            if (employeeRepo.findByEmail(request.email().trim()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
            }

            // Create new employee
            Employee employee = new Employee();
            employee.setFirstName(request.firstName().trim());
            employee.setLastName(request.lastName().trim());
            employee.setEmail(request.email().trim());
            employee.setPasswordHash(passwordEncoder.encode(request.password()));

            // Save to database
            employeeRepo.save(employee);

            // Auto-login the user after registration
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(employee.getEmail(), request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "firstName", employee.getFirstName(),
                "email", employee.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Employee employee = employeeRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        return ResponseEntity.ok(Map.of(
            "firstName", employee.getFirstName(),
            "lastName", employee.getLastName(),
            "email", employee.getEmail()
        ));
    }

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String firstName, String lastName, String email, String password) {}
}
