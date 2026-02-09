package com.example.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    //인증 유효 시간 저장
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    // 5분
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;

    //인증 번호 생성 및 이메일 발송
    public String sendVerificationCode(String email) {
        log.info("Sending verification code to : {}", email);

        String code = generateCode();

        // 인증번호 저장
        verificationCodes.put(email, code);
        expirationTimes.put(email, System.currentTimeMillis() + EXPIRATION_TIME);

        //이메일 발송
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[걸어서 맛집으로] 이메일 인증 번호");
            message.setText("인증번호: "+ code + "\n\n5분 이내에 입력해주세요!!");

            mailSender.send(message);

            log.info("verification code sent successfully to:{}", email);
            return code;
        }catch (Exception e){
            log.info("Failed to send email to: {}",email,e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    //인증번호 검증
    public boolean verifyCode(String email, String code) {
        log.info("Verifying code for: {}", email);

        // 인증 번호 확인
        String savedCode = verificationCodes.get(email);
        if (savedCode == null) {
            log.warn("No verification code found for {}", email);
            return false;
        }

        //유효시간 확인
        Long expirationTime = expirationTimes.get(email);
        if (expirationTime == null || System.currentTimeMillis() > expirationTime) {
            log.warn("Verification code expired for: {}", email);
            verificationCodes.remove(email);
            expirationTimes.remove(email);
            return false;
        }

        //인증 비교
        boolean isValid = savedCode.equals(code);

        if (isValid) {
            log.info("Verification successful for: {}", email);

            verificationCodes.remove(email);
            expirationTimes.remove(email);
        }else {
            log.warn("Invalid verification code for: {}", email);
        }

        return isValid;
    }

    // 난수 생성
    private String generateCode() {
        Random random = new Random();
        int code = 100000 +  random.nextInt(900000);
        return String.valueOf(code);
    }

}
