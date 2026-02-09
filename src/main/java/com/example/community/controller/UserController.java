package com.example.community.controller;

import com.example.community.domain.user.UserDto;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
