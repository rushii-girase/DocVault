package com.app.institutional.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank
    private String classLevel;

    @NotBlank
    private String division;

    @NotBlank
    private String rollNo;
}
