package com.healthsync.backend.dto;

public record ResetPasswordRequest(String email, String otp, String newPassword) {}
