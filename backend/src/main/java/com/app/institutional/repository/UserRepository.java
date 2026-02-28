package com.app.institutional.repository;

import com.app.institutional.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByMobileNo(String mobileNo);

    boolean existsByRollNo(String rollNo);

    java.util.List<User> findByRole(com.app.institutional.entity.enums.Role role);
}
