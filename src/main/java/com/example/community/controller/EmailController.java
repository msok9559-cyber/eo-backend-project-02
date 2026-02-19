package com.example.community.controller;

import com.example.community.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    /**
     * 이메일 인증번호 발송
     */
    @GetMapping("/send-verification")
    @ResponseBody
    public Map<String, Object> sendVerificationCode(@RequestParam String email) {
        log.info("이메일 인증 요청: {}", email);

        Map<String, Object> response = new HashMap<>();

        try {
            emailService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", email, e);
            response.put("success", false);
            response.put("message", "이메일 발송에 실패했습니다.");
        }

        return response;
    }

    /**
     * 인증번호 확인
     */
    @GetMapping("/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code) {
        log.info("인증번호 확인: email={}, code={}", email, code);

        Map<String, Object> response = new HashMap<>();

        boolean isValid = emailService.verifyCode(email, code);

        if (isValid) {
            response.put("success", true);
            response.put("message", "인증이 완료되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "인증번호가 일치하지 않거나 만료되었습니다.");
        }

        return response;
    }
}