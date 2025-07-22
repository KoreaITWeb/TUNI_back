package com.project.tuni_back.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[TUNI] Verification Code"); // 서비스 이름 포함
        message.setText("Verification Code is [" + code + "]. Please verify in 5 minutes.");
        try {
            mailSender.send(message);
            log.info("{} 주소로 인증 메일 전송 성공", toEmail);
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("메일 전송에 실패했습니다.");
        }
    }
}