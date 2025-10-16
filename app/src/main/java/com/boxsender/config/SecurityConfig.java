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

@Configuration
public class SecurityConfig {

  @Bean
AuthenticationManager authenticationManager(UserDetailsService uds,
                                            PasswordEncoder encoder) {
  DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
  provider.setUserDetailsService(uds);
  provider.setPasswordEncoder(encoder);
  return new ProviderManager(List.of(provider));
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(EmployeeRepository repo) {
    return usernameEmail -> {
      Employee e = repo.findByEmail(usernameEmail)
          .orElseThrow(() -> new UsernameNotFoundException("No user " + usernameEmail));

      return User.withUsername(e.getEmail())
          .password(e.getPasswordHash())  // already hashed
          .roles("USER")
          .build();
    };
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
  .csrf(csrf -> csrf.disable()) // fine for now for JSON posts
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/", "/index.html", "/assets/**",
                       "/api/auth/register", "/api/auth/login") // <-- add login
      .permitAll()
      .anyRequest().authenticated()
  )
  .formLogin(login -> login
      .loginPage("/index.html")
      .defaultSuccessUrl("/dashboard.html", true)
      .permitAll()
  )
  .logout(logout -> logout
      .logoutUrl("/logout")
      .logoutSuccessUrl("/index.html")
  );
    

    return http.build();
  }
}