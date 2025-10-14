package com.boxsender.app.controller;


import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")

public class AuthController{
    record RegisterReq(String firstName, String lastName, String email, String password) {}
    
    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody RegisterReq r){
    
    return Map.of("ok", true, "email", r.email());
}
}


