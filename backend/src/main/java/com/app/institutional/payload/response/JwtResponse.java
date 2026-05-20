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
    private String mobileNo;
    private String collegeName;
    private String course;
    private String classLevel;
    private String division;
    private String rollNo;

    public JwtResponse(String accessToken, Long id, String name, String email, String role, boolean emailVerified,
            String caste, String mobileNo, String collegeName, String course, String classLevel, String division, String rollNo) {
        this.token = accessToken;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.caste = caste;
        this.mobileNo = mobileNo;
        this.collegeName = collegeName;
        this.course = course;
        this.classLevel = classLevel;
        this.division = division;
        this.rollNo = rollNo;
    }
}
