package com.boxsender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Box Sender Application - Main Entry Point
 *
 * This is the main class that starts the Spring Boot application.
 * It serves as the entry point for the Box Sender package tracking system.
 *
 * @SpringBootApplication Annotation:
 * This is a convenience annotation that combines three important annotations:
 *
 * 1. @Configuration
 *    - Marks this class as a source of bean definitions
 *    - Allows Spring to configure the application context
 *
 * 2. @EnableAutoConfiguration
 *    - Tells Spring Boot to automatically configure beans based on:
 *      * Dependencies in the classpath (pom.xml)
 *      * Properties in application.properties
 *    - Examples: Automatically configures database connection, web server, security, etc.
 *
 * 3. @ComponentScan
 *    - Scans the package (com.boxsender) and sub-packages
 *    - Finds and registers all @Component, @Service, @Repository, @Controller classes
 *    - Makes them available for dependency injection
 *
 * What Happens When Application Starts:
 * 1. main() method is called by the JVM
 * 2. SpringApplication.run() initializes Spring Boot
 * 3. Spring scans for components (@Service, @Controller, @Repository, etc.)
 * 4. Auto-configuration sets up database, web server, security
 * 5. Embedded Tomcat server starts (default port 8080)
 * 6. Application is ready to handle HTTP requests
 *
 * Application Architecture:
 * - Database: MySQL/H2 (configured in application.properties)
 * - Web Server: Embedded Tomcat
 * - Security: Spring Security with session-based authentication
 * - Email: JavaMailSender with Brevo SMTP
 * - Frontend: Static HTML/CSS/JavaScript served from resources/static
 *
 * To run the application:
 * - From IDE: Run this main method
 * - From command line: mvn spring-boot:run
 * - Access at: http://localhost:8080
 */
@SpringBootApplication  // Enables Spring Boot auto-configuration and component scanning
public class AppApplication {

  /**
   * Main method - Application entry point
   *
   * This method starts the entire Spring Boot application.
   * SpringApplication.run() performs all the initialization and starts the web server.
   *
   * @param args command-line arguments (can be used to override properties)
   */
  public static void main(String[] args) {
    // Start the Spring Boot application
    // This initializes Spring context, sets up beans, and starts embedded Tomcat server
    SpringApplication.run(AppApplication.class, args);
  }
}
