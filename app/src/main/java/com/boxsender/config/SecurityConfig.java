// package com.boxsender.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.ProviderManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;

// import com.boxsender.users.Employee;
// import com.boxsender.users.EmployeeRepository;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     //BCrypt password hasher
//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     //how to load users from database
//     @Bean
//     public UserDetailsService userDetailsService(EmployeeRepository employeeRepo) {
//         return email -> {
//              // 1. Find employee in database by email
//             Employee employee = employeeRepo.findByEmail(email)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            
//             // 2. Convert Employee to Spring Security's User object
//             return User.builder()
//                 .username(employee.getEmail())                  // Username for Spring Security
//                 .password(employee.getPasswordHash())           // Hashed password
//                 .roles("USER")                        // User role/permission
//                 .build();
//         };
//     }

//     // Authentication coordinator
//     @Bean
//     @SuppressWarnings("deprecation")
//     public AuthenticationManager authenticationManager(
//         UserDetailsService userDetailsService,
//         PasswordEncoder passwordEncoder) 
//         {
//         // Create authentication provider
//         DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//         provider.setUserDetailsService(userDetailsService);     // How to load users
//         provider.setPasswordEncoder(passwordEncoder);           // How to verify passwords


//         return new ProviderManager(provider);
//     }

//     // Security rules
//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             // DISABLE CSRF for REST API
//             .csrf(csrf -> csrf.disable())

//             // CONFIGURE URL ACCESS RULES
//             .authorizeHttpRequests(auth -> auth
//                 // These URLs are public (no login needed)
//                 .requestMatchers("/", "/index.html", "/assets/**", "/api/auth/login").permitAll()
//                 // Everything else requires authentication
//                 .anyRequest().authenticated()
//             )

//             // SESSION MANAGEMENT
//             .sessionManagement(session -> session
//                 .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//             )
//             // FORM LOGIN CONFIGURATION
//             .formLogin(form -> form
//                 .loginPage("/index.html")                       // Custom login page
//                 .defaultSuccessUrl("/dashboard.html", true)        //Redirect after login
//                 .permitAll()
//             )

//             // LOGOUT CONFIGURATION
//             .logout(logout -> logout
//                 .logoutUrl("/logout")
//                 .logoutSuccessUrl("/index.html")
//                 .permitAll()
//             );

//         return http.build();
//     }
// }


package com.boxsender.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(EmployeeRepository employeeRepo) {
        return email -> {
            Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            
            // Get role from database
            String role = employee.getRole();
            if (role == null || role.isEmpty()) {
                role = "EMPLOYEE";
            }
            
            return User.builder()
                .username(employee.getEmail())
                .password(employee.getPasswordHash())
                .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
                ))
                .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Public URLs
                .requestMatchers(
                    "/", 
                    "/index.html", 
                    "/assets/**", 
                    "/api/auth/login",
                    "/api/auth/register"
                ).permitAll()
                
                // MAILROOM STAFF ONLY
                .requestMatchers("/log.html", "/pickup.html", "/reports.html").hasRole("MAILROOM_STAFF")
                
                // BOTH ROLES
                .requestMatchers("/dashboard.html", "/search.html").hasAnyRole("MAILROOM_STAFF", "EMPLOYEE")
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .formLogin(form -> form
                .loginPage("/index.html")
                .defaultSuccessUrl("/dashboard.html", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html")
                .permitAll()
            );

        return http.build();
    }
}