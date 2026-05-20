package com.app.institutional.controller;

import com.app.institutional.entity.User;
import com.app.institutional.entity.enums.CollegeName;
import com.app.institutional.entity.enums.Course;
import com.app.institutional.entity.enums.Role;
import com.app.institutional.payload.request.StaffSignupRequest;
import com.app.institutional.payload.response.MessageResponse;
import com.app.institutional.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    com.app.institutional.repository.AuditLogRepository auditLogRepository;

    @Autowired
    com.app.institutional.repository.DocumentReviewRepository documentReviewRepository;

    @Autowired
    com.app.institutional.repository.NotificationRepository notificationRepository;

    @PostMapping("/register-staff")
    public ResponseEntity<?> registerStaff(@Valid @RequestBody StaffSignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        CollegeName collegeName = null;
        Course course = null;

        try {
            if (signUpRequest.getCollegeName() != null) {
                collegeName = CollegeName.fromString(signUpRequest.getCollegeName());
            }
            if (signUpRequest.getCourse() != null) {
                course = Course.fromString(signUpRequest.getCourse());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid enum value provided. " + e.getMessage()));
        }

        User staff = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .role(Role.STAFF)
                .collegeName(collegeName)
                .course(course)
                .emailVerified(true) // Admin creates staff, implicitly verified
                .isActive(true)
                .build();

        userRepository.save(staff);

        return ResponseEntity.ok(new MessageResponse("Staff member registered successfully!"));
    }

    @GetMapping("/staff")
    public ResponseEntity<java.util.List<User>> getAllStaff() {
        return ResponseEntity.ok(userRepository.findByRole(Role.STAFF));
    }

    @PutMapping("/staff/{id}/toggle-block")
    public ResponseEntity<?> toggleBlockStaff(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        if (user.getRole() != Role.STAFF) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User is not a staff member."));
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(
                "Staff member status updated successfully: " + (user.isActive() ? "Active" : "Blocked")));
    }

    @Transactional
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        if (user.getRole() != Role.STAFF) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User is not a staff member."));
        }

        // Clean up dependencies before deleting the Staff entity
        documentReviewRepository.deleteByReviewerId(id);
        notificationRepository.deleteByRecipientId(id);
        auditLogRepository.deleteByActorId(id);

        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Staff member deleted successfully."));
    }

    @PutMapping("/staff/{id}/info")
    public ResponseEntity<?> updateStaffInfo(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        String newName = request.get("name");
        String newEmail = request.get("email");
        
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Name cannot be empty."));
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email cannot be empty."));
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        if (user.getRole() != Role.STAFF) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User is not a staff member."));
        }

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        user.setName(newName);
        user.setEmail(newEmail);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Staff info updated successfully!"));
    }
}
