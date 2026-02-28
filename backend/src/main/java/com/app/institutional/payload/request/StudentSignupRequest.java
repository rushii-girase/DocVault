package com.app.institutional.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentSignupRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String mobileNo;

    @NotBlank
    private String caste; // String representation mapped in controller

    @NotBlank
    private String collegeName;

    @NotBlank
    private String course;

    @NotBlank
    private String classLevel;

    private String division;

    @NotBlank
    private String rollNo;
}
