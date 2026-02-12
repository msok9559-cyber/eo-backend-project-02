package com.example.community.controller;

import com.example.community.domain.comment.CommentDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        Long userId = getCurrentUserId();
        commentDto.setPostId(postId);

        return commentService.create(commentDto, userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(403).body("댓글 작성 권한이 없습니다."));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> read(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        return commentService.read(commentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        Long userId = getCurrentUserId();
        commentDto.setId(commentId);
        commentDto.setPostId(postId);

        return commentService.update(commentDto, userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(403).body("댓글 수정 권한이 없습니다."));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = getCurrentUserId();

        Map<String, Object> response = new HashMap<>();

        boolean delete =  commentService.delete(commentId, userId);

        if (!delete) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");

            return ResponseEntity.status(403).body(response);
        }

        response.put("success", true);
        response.put("message", "삭제되었습니다");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> readAll(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.getList(postId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        throw new IllegalStateException("지원하지 않는 인증 사용자 타입");
    }
}
