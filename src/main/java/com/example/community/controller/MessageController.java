package com.example.community.controller;

import com.example.community.domain.message.MessageDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    /**
     * 현재 로그인한 사용자의 정보를 SecurityContextHolder에서 직접 꺼내는 메서드
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        return null; // 또는 예외 발생
    }

    // 쪽지 발송
    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody MessageDto messageDto) {
        String username = getCurrentUsername();
        messageService.sendMessage(messageDto, username);
        return ResponseEntity.ok("Message sent successfully");
    }

    // 받은 쪽지함 조회
    @GetMapping("/received")
    public ResponseEntity<Page<MessageDto>> readReceived(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        return ResponseEntity.ok(messageService.getReceivedMessages(username, pageable));
    }

    // 쪽지 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MessageDto> read(@PathVariable Long id) {
        String username = getCurrentUsername();
        return ResponseEntity.of(messageService.getMessageDetail(id, username));
    }

    // 쪽지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        String username = getCurrentUsername();
        messageService.deleteMessage(id, username);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}