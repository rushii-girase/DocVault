package com.app.institutional.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Institutional Platform - Email Verification");

        // Target Frontend URL format for email verification route
        String verificationUrl = "http://localhost:4200/verify-email?token=" + token;

        message.setText("Dear Student,\n\n" +
                "Please click on the link below to verify your email address:\n" +
                verificationUrl + "\n\n" +
                "Thank you,\n" +
                "Institutional Platform Team");

        mailSender.send(message);
    }
}
