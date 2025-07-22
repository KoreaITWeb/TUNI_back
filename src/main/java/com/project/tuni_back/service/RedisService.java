package com.project.tuni_back.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void setVerificationCode(String email, String code) {
        // 이메일을 Key로, 코드를 Value로 5분간 저장
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(email, code, Duration.ofMinutes(5));
    }

    public String getVerificationCode(String email) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(email);
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(email);
    }
}