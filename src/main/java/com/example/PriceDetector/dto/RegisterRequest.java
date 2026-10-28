package com.example.PriceDetector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email
    @NotBlank(message = "can not be empty email")
    private String email;

    @NotBlank(message = "can not be empty password")
    private String password;

}
