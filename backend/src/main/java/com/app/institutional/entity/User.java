package com.app.institutional.entity;

import com.app.institutional.entity.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Email verification fields
    @Builder.Default
    private boolean emailVerified = false;
    private String verificationToken;
    private LocalDateTime tokenExpiryDate;

    // Admin control
    @Builder.Default
    private boolean isActive = true;

    // --- Student Specific Fields ---

    @Column(unique = true)
    private String mobileNo;

    @Enumerated(EnumType.STRING)
    private CasteCategory caste;

    @Enumerated(EnumType.STRING)
    private CollegeName collegeName;

    @Enumerated(EnumType.STRING)
    private Course course;

    @Enumerated(EnumType.STRING)
    private ClassLevel classLevel;

    private String division;

    @Column(unique = true)
    private String rollNo;
}
