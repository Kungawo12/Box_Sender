package com.boxsender.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

/**
 * Security Configuration Class
 *
 * This class configures Spring Security for the Box Sender application.
 * It sets up authentication, authorization, password encryption, and session management.
 *
 * Key Security Features Configured:
 * 1. User authentication using email and password
 * 2. Password encryption with BCrypt hashing algorithm
 * 3. Session-based authentication (not JWT)
 * 4. URL access control (public vs. protected endpoints)
 * 5. Form-based login and logout
 *
 * Spring Security Concepts:
 * - Authentication: Verifying who the user is (login)
 * - Authorization: Verifying what the user can access (permissions)
 * - @Bean: Methods marked with @Bean create objects managed by Spring
 * - @Configuration: Marks this class as a source of bean definitions
 *
 * Security Flow:
 * 1. User submits email/password
 * 2. AuthenticationManager validates credentials
 * 3. UserDetailsService loads user from database
 * 4. PasswordEncoder compares hashed passwords
 * 5. If valid, user is authenticated and session is created
 */
@Configuration  // Tells Spring this class contains bean definitions
public class SecurityConfig {

  /**
   * Authentication Manager Bean
   *
   * The AuthenticationManager is the core component that handles authentication requests.
   * It coordinates the authentication process by delegating to authentication providers.
   *
   * This configuration uses DaoAuthenticationProvider which:
   * - Loads user details from database via UserDetailsService
   * - Compares passwords using the PasswordEncoder
   * - Returns an Authentication object if credentials are valid
   *
   * Flow when user logs in:
   * 1. AuthenticationManager receives login request
   * 2. DaoAuthenticationProvider loads user from database
   * 3. Compares submitted password (after hashing) with stored hash
   * 4. If match, authentication succeeds
   *
   * @param uds UserDetailsService to load user data from database
   * @param encoder PasswordEncoder to verify passwords
   * @return configured AuthenticationManager
   */
  @Bean
  AuthenticationManager authenticationManager(UserDetailsService uds,
                                              PasswordEncoder encoder) {
    // DaoAuthenticationProvider authenticates against database
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);  // How to load users
    provider.setPasswordEncoder(encoder);  // How to verify passwords
    // ProviderManager manages multiple authentication providers
    return new ProviderManager(List.of(provider));
  }

  /**
   * Password Encoder Bean
   *
   * BCrypt is a password hashing function designed for secure password storage.
   * It automatically handles:
   * - Salt generation (random data added to passwords)
   * - Multiple rounds of hashing (makes brute-force attacks slow)
   * - Secure comparison to prevent timing attacks
   *
   * Why BCrypt?
   * - Industry standard for password hashing
   * - Computationally expensive (protects against brute-force)
   * - Automatically generates unique salt for each password
   * - Future-proof (can increase work factor as computers get faster)
   *
   * NEVER store plain text passwords - always use BCrypt or similar!
   *
   * @return BCryptPasswordEncoder for hashing and verifying passwords
   */
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * User Details Service Bean
   *
   * UserDetailsService is Spring Security's interface for loading user data.
   * This implementation loads Employee data from the database and converts it
   * to Spring Security's UserDetails format.
   *
   * This is a lambda function that:
   * 1. Takes an email address (username in our case)
   * 2. Queries the database for that employee
   * 3. Converts Employee entity to UserDetails object
   * 4. Throws exception if user not found
   *
   * The returned UserDetails contains:
   * - Username (email in our case)
   * - Password (BCrypt hash)
   * - Roles/authorities (permissions)
   * - Account status (enabled, locked, expired, etc.)
   *
   * @param repo EmployeeRepository to query the database
   * @return UserDetailsService lambda that loads users by email
   */
  @Bean
  public UserDetailsService userDetailsService(EmployeeRepository repo) {
    // Lambda that takes email and returns UserDetails
    return usernameEmail -> {
      // Look up employee by email
      Employee e = repo.findByEmail(usernameEmail)
          .orElseThrow(() -> new UsernameNotFoundException("No user " + usernameEmail));

      // Convert Employee to Spring Security's UserDetails format
      return User.withUsername(e.getEmail())  // Username is email
          .password(e.getPasswordHash())  // Password is already BCrypt hashed
          .roles("USER")  // Assign USER role (could extend for ADMIN, etc.)
          .build();
    };
  }

  /**
   * Security Filter Chain Bean
   *
   * This configures the security rules for HTTP requests.
   * It defines which URLs require authentication and which are public.
   *
   * Security Configuration:
   * 1. CSRF Protection: Disabled for JSON API endpoints
   *    - CSRF (Cross-Site Request Forgery) protection is typically for form submissions
   *    - For REST APIs with JSON, it's common to disable CSRF
   *    - Would use CSRF tokens if using traditional form submissions
   *
   * 2. Authorization Rules (which URLs need authentication):
   *    - Public (permitAll): login page, static assets, register/login endpoints
   *    - Protected (authenticated): all other requests require login
   *
   * 3. Form Login: Traditional form-based authentication
   *    - Login page: /index.html
   *    - Success redirect: /dashboard.html
   *    - Spring Security handles the authentication automatically
   *
   * 4. Logout: How to log users out
   *    - Logout URL: /logout
   *    - After logout, redirect to: /index.html
   *    - Clears the session and authentication
   *
   * Security Flow:
   * 1. User tries to access protected URL (e.g., /dashboard.html)
   * 2. Spring Security checks if user is authenticated
   * 3. If not authenticated, redirects to /index.html
   * 4. After successful login, redirects to originally requested page or dashboard
   *
   * @param http HttpSecurity object to configure security rules
   * @return SecurityFilterChain with all configured security rules
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // CSRF: Cross-Site Request Forgery protection
      // Disabled here because we're using JSON APIs, not form submissions
      .csrf(csrf -> csrf.disable())

      // Authorization rules: which URLs require authentication
      .authorizeHttpRequests(auth -> auth
          // Public URLs - anyone can access without logging in
          .requestMatchers("/", "/index.html", "/assets/**",
                           "/api/auth/register", "/api/auth/login")
          .permitAll()

          // All other URLs require authentication
          .anyRequest().authenticated()
      )

      // Form-based login configuration
      .formLogin(login -> login
          .loginPage("/index.html")  // Custom login page
          .defaultSuccessUrl("/dashboard.html", true)  // Redirect after login
          .permitAll()  // Allow everyone to see login page
      )

      // Logout configuration
      .logout(logout -> logout
          .logoutUrl("/logout")  // URL to trigger logout
          .logoutSuccessUrl("/index.html")  // Redirect after logout
      );

    // Build and return the configured security filter chain
    return http.build();
  }
}