package com.boxsender.app.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo Controller
 *
 * This is a simple demonstration controller to test that the Spring Boot
 * application is running correctly and the REST API is working.
 *
 * Base URL: /api
 *
 * This controller can be used to verify:
 * - Spring Boot application is running
 * - REST endpoints are accessible
 * - JSON serialization is working correctly
 *
 * Typically used during development for testing purposes.
 */
@RestController  // Marks this as a REST controller that returns JSON data
@RequestMapping("/api")  // Base path for all endpoints in this controller
public class DemoController {

  /**
   * Simple test endpoint to verify the API is working
   *
   * HTTP Endpoint: GET /api/hello
   * Authentication: Not required (public endpoint)
   *
   * Returns a simple greeting message in JSON format.
   * Useful for testing that the server is running and responding correctly.
   *
   * @return Map containing a greeting message
   *         Example response: {"msg": "Hi from Spring Boot"}
   */
  @GetMapping("/hello")  // Maps to GET requests at /api/hello
  public Map<String,String> hello() {
    return Map.of("msg", "Hi from Spring Boot 👋");
  }
}