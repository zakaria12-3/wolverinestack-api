package com.example.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterUserDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "Password must include an uppercase letter, a lowercase letter, a number, and a symbol"
    )
    private String password;

    @NotBlank(message = "Username is required")
    private String username;

    private String role;

    // Fitness onboarding fields (optional at signup — can be completed later)
    private String gender;
    private String fitnessGoal;
    private String activityLevel;
    private Double weightKg;
    private Double heightCm;
    private LocalDate dateOfBirth;

    private String gymName;
    private String gymAddress;
    private String gymCity;
    private String gymCountry;
    private String gymWebsite;
    private String gymPhone;
    private String gymDescription;
    private String gymOpeningHours;
    private String jobTitle;
    private String phone;
}
