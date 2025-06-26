package com.polyHub.member.service;

public interface EmailService {
    boolean sendVerificationCode(String email);
}
