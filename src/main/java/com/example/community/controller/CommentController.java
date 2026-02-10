package com.example.community.controller;

import com.example.community.domain.comment.CommentDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<CommentDto> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        Long userId = getCurrentUserId();
        commentDto.setPostId(postId);

        return ResponseEntity.of(
                commentService.create(commentDto, userId)
        );
    }

    /* =====================
       댓글 단건 조회
     ===================== */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> read(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.of(
                commentService.read(commentId)
        );
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        Long userId = getCurrentUserId();

        commentDto.setId(commentId);
        commentDto.setPostId(postId);

        return ResponseEntity.of(
                commentService.update(commentDto, userId)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = getCurrentUserId();

        boolean deleted = commentService.delete(commentId, userId);

        if (!deleted) {
            return ResponseEntity.notFound().build(); // 또는 403
        }

        return ResponseEntity.ok().build();
    }


    @GetMapping
    public ResponseEntity<List<CommentDto>> readAll(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                commentService.getList(postId)
        );
    }

    /* =====================
       Validation 예외 처리
     ===================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(errors);
    }

    /* =====================
       Security 사용자 추출
     ===================== */
    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

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
