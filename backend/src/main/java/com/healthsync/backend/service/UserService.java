package com.healthsync.backend.service;

import com.healthsync.backend.dto.*;
import com.healthsync.backend.exception.EmailAlreadyExistsException;
import com.healthsync.backend.model.User;
import com.healthsync.backend.repository.UserRepository;
import com.healthsync.backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public MessageResponse register(RegisterDto registerDto) {
        userRepository.findByEmail(registerDto.email()).ifPresent(u -> {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        });

        User user = new User();
        user.setFullName(registerDto.fullName());
        user.setEmail(registerDto.email());
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        user.setVerified(false);

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGenerationTime(LocalDateTime.now());

        emailService.sendOtpEmail(user.getEmail(), otp);
        userRepository.save(user);

        return new MessageResponse("OTP sent to your email. Please verify to complete registration.");
    }

    public AuthResponse verifyOtp(VerificationRequest verificationRequest) {
        User user = userRepository.findByEmail(verificationRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified.");
        }

        if (user.getOtp() == null || !user.getOtp().equals(verificationRequest.otp())) {
            throw new RuntimeException("Invalid OTP.");
        }

        if (user.getOtpGenerationTime().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired.");
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpGenerationTime(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Account verified successfully. You are now logged in.");
    }
    
    public MessageResponse resendOtp(ResendOtpRequest resendOtpRequest) {
        User user = userRepository.findByEmail(resendOtpRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified.");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGenerationTime(LocalDateTime.now());

        emailService.sendOtpEmail(user.getEmail(), otp);
        userRepository.save(user);

        return new MessageResponse("A new OTP has been sent to your email.");
    }

    public AuthResponse login(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );

        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (!user.isVerified()) {
            throw new RuntimeException("Account not verified. Please verify your OTP first.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Logged in successfully.");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGenerationTime(LocalDateTime.now());

        emailService.sendOtpEmail(user.getEmail(), otp);
        userRepository.save(user);

        return new MessageResponse("OTP for password reset has been sent to your email.");
    }

    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(resetPasswordRequest.otp())) {
            throw new RuntimeException("Invalid OTP.");
        }

        if (user.getOtpGenerationTime().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired.");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        user.setOtp(null);
        user.setOtpGenerationTime(null);
        userRepository.save(user);

        return new MessageResponse("Password has been reset successfully.");
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
