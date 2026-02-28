package com.app.institutional.payload.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean emailVerified;
    private String caste;

    public JwtResponse(String accessToken, Long id, String name, String email, String role, boolean emailVerified,
            String caste) {
        this.token = accessToken;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.caste = caste;
    }
}
