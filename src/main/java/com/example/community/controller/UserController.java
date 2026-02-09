package com.example.community.controller;

import com.example.community.domain.user.UserDto;
import com.example.community.service.EmailService;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final EmailService emailService;


    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public void login() {
        // login.html 반환
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public void signup() {
        // signup.html 반환
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(@Valid UserDto userDto) {
        userService.create(userDto);
        return "redirect:/login";
    }
    /**
     * 아이디 중복 체크 
     */
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return !userService.existsByUsername(username);
    }

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/check-nickname")
    @ResponseBody
    public boolean checkNickname(@RequestParam String nickname) {
        return !userService.existsByNickname(nickname);
    }

    /**
     * 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleArgumentException(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "redirect:/signup";
    }

    /**
     * 이메일 인증번호 발송
     */
    @GetMapping("/send-verification-email")
    @ResponseBody
    public Map<String, Object> sendVerificationEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            String code = emailService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
            // response.put("code", code); // 테스트용 (실제로는 제거!)

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이메일 발송에 실패했습니다.");
        }

        return response;
    }

    /**
     * 이메일 인증번호 확인
     */
    @GetMapping("/verify-email-code")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code) {

        Map<String, Object> response = new HashMap<>();

        boolean isValid = emailService.verifyCode(email, code);

        if (isValid) {
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "인증번호가 올바르지 않거나 만료되었습니다.");
        }

        return response;
    }
}
