package com.example.PriceDetector.controller;

import com.example.PriceDetector.dto.AuthResponse;
import com.example.PriceDetector.dto.LoginRequest;
import com.example.PriceDetector.dto.RegisterRequest;
import com.example.PriceDetector.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {
private final AuthService authService;

@PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request)
{
    return authService.register(request);
}

@PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request)
{
    return authService.login(request);
}

}
