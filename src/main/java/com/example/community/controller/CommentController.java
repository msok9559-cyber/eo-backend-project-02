package com.example.community.controller;

import com.example.community.domain.comment.CommentDto;
import com.example.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            @RequestParam Long userId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        // postId는 URL 기준이므로 DTO에 세팅
        commentDto.setPostId(postId);

        return ResponseEntity.of(
                commentService.create(commentDto, userId)
        );
    }

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
            @RequestParam Long userId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        commentDto.setId(commentId);
        commentDto.setPostId(postId);

        return ResponseEntity.of(
                commentService.update(commentDto, userId)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        return commentService.delete(commentId, userId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(403).build();
    }


    @GetMapping
    public ResponseEntity<List<CommentDto>> readAll(@PathVariable Long postId) {
        return ResponseEntity.ok(
                commentService.getList(postId)
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(errors);
    }
}
