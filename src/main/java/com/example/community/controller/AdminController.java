package com.example.community.controller;

import com.example.community.domain.user.UserDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.community.security.CustomUserDetails;
import com.example.community.domain.board.BoardDto;
import com.example.community.domain.post.PostDto;
import com.example.community.domain.comment.CommentDto;
import com.example.community.service.BoardService;
import com.example.community.service.PostService;
import com.example.community.service.CommentService;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;


import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;
    private final BoardService boardService;
    private final PostService postService;
    private final CommentService commentService;

    //관리자 대시보드 페이지
    @GetMapping("/dashboard")
    public String dashboard(){
        return "admin/dashboard";
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(required = false) String keyword) {

        log.info("Get users - page: {}, size: {}, role: {}, keyword: {}", page, size, role, keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> users = adminService.getAllUsers(pageable, role, keyword);
        return ResponseEntity.ok(users);
    }



        /**
         * 사용자 상세 조회 (API)
         */
    @GetMapping("/users/{userId}")
    @ResponseBody
    public ResponseEntity<UserDto> getUserDetail(@PathVariable Long userId) {
        log.info("Get user detail - userId: {}", userId);

        UserDto user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 권한 변경 (API)
     */
    @PatchMapping("/users/{userId}/role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String role,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Change user role - userId: {}, role: {}, reason: {}", userId, role, reason);

        Map<String, Object> response = new HashMap<>();

        try {
            Long adminId = null;
            if (currentUser != null && currentUser.getUser() != null) {
                adminId = currentUser.getUser().getId();
            }

            adminService.changeUserRole(userId, role, reason, adminId);

            response.put("success", true);
            response.put("message", "권한이 변경되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException | IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 정지 (API)
     */
    @PostMapping("/users/{userId}/ban")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> banUser(
            @PathVariable Long userId,
            @RequestParam String reason,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Ban user - userId: {}, reason: {}, duration: {}", userId, reason, duration);

        Map<String, Object> response = new HashMap<>();

        try {
            Long adminId = null;
            if (currentUser != null && currentUser.getUser() != null) {
                adminId = currentUser.getUser().getId();
            }

            adminService.banUser(userId, reason, duration, note, adminId);

            response.put("success", true);
            response.put("message", "사용자가 정지되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 활성화 (API)
     */
    @PostMapping("/users/{userId}/activate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long userId) {
        log.info("Activate user - userId: {}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            adminService.activateUser(userId);

            response.put("success", true);
            response.put("message", "사용자가 활성화되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 비활성화 (API)
     */
    @PostMapping("/users/{userId}/deactivate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long userId) {
        log.info("Deactivate user - userId: {}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            adminService.deactivateUser(userId);

            response.put("success", true);
            response.put("message", "사용자가 비활성화되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



}
