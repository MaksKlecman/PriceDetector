package com.example.PriceDetector.service;

import com.example.PriceDetector.dto.AuthResponse;
import com.example.PriceDetector.dto.LoginRequest;
import com.example.PriceDetector.dto.RegisterRequest;
import com.example.PriceDetector.model.Role;
import com.example.PriceDetector.model.User;
import com.example.PriceDetector.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


   public AuthResponse register(RegisterRequest request)
   {
       if (userRepository.findByEmail(request.getEmail()).isPresent()) {
           throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
       }
       User user = new User();
       user.setEmail(request.getEmail());
       user.setPassword(passwordEncoder.encode(request.getPassword()));
       user.setRole(Role.USER);
       userRepository.save(user);
       String token = jwtUtil.generateToken(request.getEmail());
       AuthResponse response = new AuthResponse();
       response.setEmail(user.getEmail());
       response.setToken(token);
       return response;


   }

   public  AuthResponse login(LoginRequest request)
   {
       User user = userRepository.findByEmail(request.getEmail())
               .orElseThrow(() ->  new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

       if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
       {
           throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");

       }

       String token = jwtUtil.generateToken(request.getEmail());
       AuthResponse response = new AuthResponse();
       response.setToken(token);
       response.setEmail(user.getEmail());
       return response;


   }
}
