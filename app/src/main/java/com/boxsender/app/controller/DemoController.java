package com.boxsender.app.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

  @GetMapping("/hello")
  public Map<String,String> hello() {
    return Map.of("msg", "Hi from Spring Boot 👋");
  }
}