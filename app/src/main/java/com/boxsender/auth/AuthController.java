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

/**
 * Authentication Controller
 *
 * This REST controller handles all authentication-related operations:
 * - User registration (creating new employee accounts)
 * - User login (authenticating existing employees)
 * - User logout (ending sessions)
 * - Getting current user information
 *
 * Base URL: /api/auth
 *
 * Authentication Flow:
 * 1. Registration: User submits details -> Password hashed with BCrypt -> Saved to DB -> Auto-login
 * 2. Login: User submits credentials -> AuthenticationManager validates -> Session created
 * 3. Session: Authentication stored in HTTP session (server-side)
 * 4. Logout: Session invalidated -> User redirected to login page
 *
 * Security Notes:
 * - Passwords are NEVER stored in plain text (always BCrypt hashed)
 * - Sessions are managed server-side (not JWT in this implementation)
 * - Failed login attempts throw authentication exceptions
 * - Email uniqueness is enforced during registration
 */
@RestController  // Marks this as a REST API controller
@RequestMapping("/api/auth")  // Base URL for all auth endpoints
public class AuthController {

  // Dependencies injected via constructor
  private final EmployeeRepository repo;         // Database access for employees
  private final PasswordEncoder encoder;         // BCrypt password hashing
  private final AuthenticationManager authManager;  // Handles authentication

  /**
   * Constructor-based Dependency Injection
   * Spring automatically provides these dependencies
   *
   * @param repo repository for employee database operations
   * @param encoder BCrypt encoder for password hashing
   * @param authManager manager for handling authentication requests
   */
  public AuthController(EmployeeRepository repo, PasswordEncoder encoder, AuthenticationManager authManager) {
    this.repo = repo;
    this.encoder = encoder;
    this.authManager = authManager;
  }

  /**
   * Get Current User Information
   *
   * HTTP Endpoint: GET /api/auth/me
   * Authentication: Required (user must be logged in)
   *
   * Returns information about the currently logged-in employee.
   * The Authentication object is automatically provided by Spring Security
   * based on the current session.
   *
   * Frontend uses this to:
   * - Display user's name in the UI
   * - Verify the user is still logged in
   * - Get user details without storing them client-side
   * - Determine user's role for access control
   *
   * @param auth Authentication object containing logged-in user's info (injected by Spring)
   * @return Map with user's first name, last name, email, and role
   */
  @org.springframework.web.bind.annotation.GetMapping("/me")
  public Map<String, Object> me(Authentication auth) {
    // auth.getName() returns the email of the logged-in user
    // This is configured in SecurityConfig's UserDetailsService
    var emp = repo.findByEmail(auth.getName()).orElseThrow();

    // Return user information as JSON including role for access control
    return Map.of(
        "firstName", emp.getFirstName(),
        "lastName",  emp.getLastName(),
        "email",     emp.getEmail(),
        "role",      emp.getRole()  // Added for role-based UI controls
    );
  }

  /**
   * Register a New Employee
   *
   * HTTP Endpoint: POST /api/auth/register
   * Authentication: Not required (public endpoint)
   *
   * Creates a new employee account with the following steps:
   * 1. Validates email is unique (not already registered)
   * 2. Hashes password using BCrypt (NEVER store plain text!)
   * 3. Saves new employee to database
   * 4. Automatically logs the user in (convenience feature)
   * 5. Creates session for the new user
   *
   * Request Body (JSON):
   * {
   *   "firstName": "John",
   *   "lastName": "Doe",
   *   "email": "john.doe@example.com",
   *   "password": "securePassword123"
   * }
   *
   * @param body RegisterRequest containing user details
   * @param request HttpServletRequest to create session
   * @return ResponseEntity - 200 OK if success, 400 Bad Request if email exists
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest body, HttpServletRequest request) {
    // Step 1: Check if email already exists in database
    if (repo.findByEmail(body.email()).isPresent()) {
      return ResponseEntity.badRequest().body("Email already used");
    }

    // Step 2: Create new Employee object
    Employee e = new Employee();
    e.setFirstName(body.firstName());
    e.setLastName(body.lastName());
    e.setEmail(body.email());

    // Step 3: Hash password with BCrypt before storing
    // BCrypt automatically generates salt and creates secure hash
    e.setPasswordHash(encoder.encode(body.password()));

    // Step 3.5: Set default role to EMPLOYEE
    // Even though Employee entity has default value, explicitly set it for clarity
    e.setRole("EMPLOYEE");

    // Step 4: Save employee to database
    repo.save(e);

    // Step 5: Automatically log in the newly registered user
    // Create unauthenticated token with credentials
    Authentication loginReq = UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password());
    // Authenticate the credentials
    Authentication auth = authManager.authenticate(loginReq);
    // Save authentication in HTTP session
    saveInSession(auth, request);

    return ResponseEntity.ok().build();
  }

  /**
   * Login an Existing Employee
   *
   * HTTP Endpoint: POST /api/auth/login
   * Authentication: Not required (public endpoint)
   *
   * Authenticates an employee and creates a session.
   *
   * Process:
   * 1. Receives email and password from frontend
   * 2. Creates an unauthenticated token with credentials
   * 3. AuthenticationManager validates credentials:
   *    - Loads user from database via UserDetailsService
   *    - Compares password hashes using BCrypt
   *    - Throws exception if credentials are invalid
   * 4. If valid, saves authentication in HTTP session
   * 5. Returns success response
   *
   * Request Body (JSON):
   * {
   *   "email": "john.doe@example.com",
   *   "password": "securePassword123"
   * }
   *
   * On failure: Spring Security throws BadCredentialsException (401 Unauthorized)
   *
   * @param body LoginRequest containing email and password
   * @param request HttpServletRequest to create session
   * @return ResponseEntity - 200 OK if success, 401 if credentials invalid
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request) {
    // Step 1: Create unauthenticated token with user's credentials
    Authentication loginReq = UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password());

    // Step 2: Authenticate the credentials
    // This triggers UserDetailsService to load user and PasswordEncoder to verify password
    // Throws BadCredentialsException if invalid
    Authentication auth = authManager.authenticate(loginReq);

    // Step 3: Save authentication in HTTP session
    saveInSession(auth, request);

    // Step 4: Return success response
    return ResponseEntity.ok().build();
  }

  /**
   * Logout Current User
   *
   * HTTP Endpoint: POST /api/auth/logout
   * Authentication: Not strictly required, but typically called when logged in
   *
   * Ends the user's session and clears authentication context.
   *
   * Process:
   * 1. Invalidates the HTTP session (destroys all session data)
   * 2. Clears the SecurityContext (removes authentication info)
   * 3. Returns success response
   *
   * After logout:
   * - User must log in again to access protected endpoints
   * - Session ID becomes invalid
   * - Server forgets all session data for this user
   *
   * @param request HttpServletRequest containing the session to invalidate
   * @return ResponseEntity - 200 OK
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    // Step 1: Invalidate the session (destroys session data)
    request.getSession().invalidate();

    // Step 2: Clear Spring Security context (removes authentication)
    SecurityContextHolder.clearContext();

    // Step 3: Return success response
    return ResponseEntity.ok().build();
  }

  /**
   * Helper Method: Save Authentication in Session
   *
   * This method stores the authentication information in the HTTP session.
   * Session-based authentication means the server remembers who is logged in.
   *
   * Process:
   * 1. Create empty SecurityContext
   * 2. Add authentication to context
   * 3. Store context in HTTP session with Spring Security's standard key
   *
   * Why use sessions?
   * - Server-side storage (more secure than client-side tokens)
   * - Automatic timeout and cleanup
   * - Easy to invalidate (just destroy session)
   * - No need to pass tokens in every request
   *
   * Alternative: JWT (JSON Web Tokens) would store auth client-side instead
   *
   * @param auth Authentication object from successful login
   * @param request HttpServletRequest to get/create session
   */
  private void saveInSession(Authentication auth, HttpServletRequest request) {
    // Create new empty security context
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    // Add authentication to context
    context.setAuthentication(auth);

    // Store context in HTTP session
    // getSession(true) creates session if it doesn't exist
    // Uses Spring Security's standard session attribute key
    request.getSession(true).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
  }

  /**
   * Data Transfer Object for Registration Requests
   *
   * Defines the structure of JSON for user registration.
   * Java record provides immutable data class with automatic getters.
   *
   * @param firstName user's first name
   * @param lastName user's last name
   * @param email user's email (must be unique)
   * @param password plain text password (will be hashed before storage)
   */
  public record RegisterRequest(String firstName, String lastName, String email, String password) {}

  /**
   * Data Transfer Object for Login Requests
   *
   * Defines the structure of JSON for user login.
   *
   * @param email user's email address
   * @param password plain text password (compared with stored hash)
   */
  public record LoginRequest(String email, String password) {}
}