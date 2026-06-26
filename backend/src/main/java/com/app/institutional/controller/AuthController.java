package com.app.institutional.controller;

import com.app.institutional.entity.User;
import com.app.institutional.entity.enums.*;
import com.app.institutional.payload.request.LoginRequest;
import com.app.institutional.payload.request.StudentSignupRequest;
import com.app.institutional.payload.request.UpdateProfileRequest;
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

@CrossOrigin(origins = "*", maxAge = 3600)
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
                userDetails.getCaste(),
                userOpt.get().getMobileNo(),
                userOpt.get().getCollegeName() != null ? userOpt.get().getCollegeName().getDisplayName() : null,
                userOpt.get().getCourse() != null ? userOpt.get().getCourse().getDisplayName() : null,
                userOpt.get().getClassLevel() != null ? userOpt.get().getClassLevel().name() : null,
                userOpt.get().getDivision(),
                userOpt.get().getRollNo()
        ));
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
    public ResponseEntity<java.util.List<User>> getAllStudents(Authentication authentication) {
        User requester = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        java.util.List<User> students = userRepository.findByRole(com.app.institutional.entity.enums.Role.STUDENT);
        
        if (requester.getRole() == com.app.institutional.entity.enums.Role.STAFF && requester.getCollegeName() != null) {
            students = students.stream()
                .filter(s -> s.getCollegeName() == requester.getCollegeName())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return ResponseEntity.ok(students);
    }

    @PutMapping("/student/update-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateStudentProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found."));
        }

        User user = userOpt.get();
        if (user.getRole() != Role.STUDENT) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Only students can update these details."));
        }

        try {
            ClassLevel classLevel = ClassLevel.valueOf(updateRequest.getClassLevel().toUpperCase());
            
            // Check if roll number is changed and if new roll number exists
            if (updateRequest.getRollNo() != null && !updateRequest.getRollNo().trim().isEmpty() 
                && !updateRequest.getRollNo().equals(user.getRollNo())) {
                if (userRepository.existsByRollNo(updateRequest.getRollNo())) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Roll Number is already registered!"));
                }
                user.setRollNo(updateRequest.getRollNo());
            }

            user.setClassLevel(classLevel);
            user.setDivision(updateRequest.getDivision());
            userRepository.save(user);

            // Generate a fresh JWT with updated details
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            
            // Re-authenticate to get updated details in context? Not strictly necessary if we just build the response.
            // But let's return the new JwtResponse manually since we have the updated user object.
            // Note: we'd ideally regenerate the token if roles/vital claims changed, 
            // but since we only changed class/division and JWT doesn't hold these (only in response body),
            // we can just reuse the token or generate a new one if we want.
            // Generating a new token is cleaner to refresh expiry:
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    role,
                    userDetails.isEmailVerified(),
                    userDetails.getCaste(),
                    user.getMobileNo(),
                    user.getCollegeName() != null ? user.getCollegeName().getDisplayName() : null,
                    user.getCourse() != null ? user.getCourse().getDisplayName() : null,
                    user.getClassLevel() != null ? user.getClassLevel().name() : null,
                    user.getDivision(),
                    user.getRollNo()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid class level."));
        }
    }
}
