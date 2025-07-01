package com.polyHub.config;

public class SecurityUrlConstants {
    public static final String[] PERMIT_ALL_URLS = {
            "/",
            "/login",
            "/register",
            "/css/**",
            "/member/face-register",
            "/js/**",
            "/member/sendVerificationCode",
            "/member/verifyCode",
            "/api/face-register",
            "/api/face-register/",
            "/api/face-login",
            "/face-login",
            "/member/register-success",
            "/find-id",
            "/find-password",
            "/mypage/change-password",
            "/member/withdraw-success",
            "/withdraw-success",
            "/api/chatbot-proxy",
            "/bookSearch",
            "/api/library/search"
    };
}