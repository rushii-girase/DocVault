package com.app.institutional.config;

import com.app.institutional.entity.User;
import com.app.institutional.entity.enums.Role;
import com.app.institutional.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@institutional.edu")) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email("admin@institutional.edu")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Default Admin User Created:");
            System.out.println("Email: admin@institutional.edu");
            System.out.println("Password: admin123");
        }

        // Programmatic database patch: Incase previous users were accidentally seeded
        // as inactive
        java.util.List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (!u.isActive()) {
                u.setActive(true);
                userRepository.save(u);
                System.out.println("Patched User to Active: " + u.getEmail());
            }
        }
    }
}
