package com.app.institutional.controller;

import com.app.institutional.entity.User;
import com.app.institutional.entity.enums.*;
import com.app.institutional.payload.request.LoginRequest;
import com.app.institutional.payload.request.StudentSignupRequest;
import com.app.institutional.payload.response.JwtResponse;
import com.app.institutional.payload.response.MessageResponse;
import com.app.institutional.repository.UserRepository;
import com.app.institutional.security.jwt.JwtUtils;
import com.app.institutional.security.services.UserDetailsImpl;
import com.app.institutional.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent() && !userOpt.get().isEmailVerified()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is not verified. Please check your inbox."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                role,
                userDetails.isEmailVerified(),
                userDetails.getCaste()));
    }

    @PostMapping("/register-student")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody StudentSignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        if (userRepository.existsByMobileNo(signUpRequest.getMobileNo())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Mobile Number is already in use!"));
        }

        if (userRepository.existsByRollNo(signUpRequest.getRollNo())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Roll Number is already registered!"));
        }

        CollegeName collegeName;
        Course course;
        CasteCategory caste;
        ClassLevel classLevel;

        try {
            collegeName = CollegeName.fromString(signUpRequest.getCollegeName());
            course = Course.fromString(signUpRequest.getCourse());
            caste = CasteCategory.valueOf(signUpRequest.getCaste().toUpperCase());
            classLevel = ClassLevel.valueOf(signUpRequest.getClassLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid enum value provided. " + e.getMessage()));
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .role(Role.STUDENT)
                .mobileNo(signUpRequest.getMobileNo())
                .caste(caste)
                .collegeName(collegeName)
                .course(course)
                .classLevel(classLevel)
                .division(signUpRequest.getDivision())
                .rollNo(signUpRequest.getRollNo())
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .tokenExpiryDate(LocalDateTime.now().plusHours(24))
                .isActive(true)
                .build();

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        } catch (Exception e) {
            System.err.println("Failed to send email. " + e.getMessage());
        }

        return ResponseEntity.ok(new MessageResponse(
                "Student registered successfully! Please check your email for the verification link."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid verification token."));
        }

        User user = userOpt.get();

        if (user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Verification token expired."));
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Email successfully verified! You can now login."));
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<java.util.List<User>> getAllStudents() {
        return ResponseEntity.ok(userRepository.findByRole(com.app.institutional.entity.enums.Role.STUDENT));
    }
}
