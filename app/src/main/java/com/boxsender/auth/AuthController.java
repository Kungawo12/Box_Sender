package com.boxsender.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final EmployeeRepository repo;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;

  public AuthController(EmployeeRepository repo, PasswordEncoder encoder, AuthenticationManager authManager) {
    this.repo = repo;
    this.encoder = encoder;
    this.authManager = authManager;
  }

  @org.springframework.web.bind.annotation.GetMapping("/me")
public Map<String, Object> me(Authentication auth) {
  // auth.getName() is the email of the logged-in user
  var emp = repo.findByEmail(auth.getName()).orElseThrow();
  return Map.of(
      "firstName", emp.getFirstName(),
      "lastName",  emp.getLastName(),
      "email",     emp.getEmail()
  );
  }

  // Register -> save user -> immediately log them in -
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest body, HttpServletRequest request) {
    if (repo.findByEmail(body.email()).isPresent()) {
      return ResponseEntity.badRequest().body("Email already used");
    }
    Employee e = new Employee();
    e.setFirstName(body.firstName());
    e.setLastName(body.lastName());
    e.setEmail(body.email());
    e.setPasswordHash(encoder.encode(body.password())); // BCrypt!
    repo.save(e);

    // auto-login after register
    Authentication loginReq = UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password());
    Authentication auth = authManager.authenticate(loginReq);
    saveInSession(auth, request);

    return ResponseEntity.ok().build();
  }

  // Login -> authenticate -> save in session -> 200 OK
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request) {
    Authentication loginReq = UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password());
    Authentication auth = authManager.authenticate(loginReq);
    saveInSession(auth, request);
    return ResponseEntity.ok().build();
  }

  // Logout -> clear session -> 200 OK
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    request.getSession().invalidate();
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok().build();
  }

  private void saveInSession(Authentication auth, HttpServletRequest request) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    request.getSession(true).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
  }

  public record RegisterRequest(String firstName, String lastName, String email, String password) {}
  public record LoginRequest(String email, String password) {}
}