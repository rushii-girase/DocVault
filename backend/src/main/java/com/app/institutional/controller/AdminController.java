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

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

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

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        if (user.getRole() != Role.STAFF) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User is not a staff member."));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Staff member deleted successfully."));
    }
}
