package com.boxsender.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AuthController(AuthenticationManager authenticationManager,
                         EmployeeRepository employeeRepo) {
        this.authenticationManager = authenticationManager;
        this.employeeRepo = employeeRepo;
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
}
