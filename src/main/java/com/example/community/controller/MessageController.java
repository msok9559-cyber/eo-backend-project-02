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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
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
        return null;
    }

    /**
     * [테스트용 임시 수정] 받은 쪽지함 조회
     * HTML 파일이 없는 상태에서 테스트하기 위해 @ResponseBody를 추가하여 JSON 데이터를 직접 반환함
     */
    @GetMapping("/received")
    @ResponseBody
    public ResponseEntity<Page<MessageDto>> receivedPage(@RequestParam(defaultValue = "1") int page) {
        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));

        // 서비스 로직 호출
        Page<MessageDto> messagePage = messageService.getMessages("received", username, pageable);

        return ResponseEntity.ok(messagePage);
    }

    /**
     * 목록 조회 (API)
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Page<MessageDto>> list(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page) {

        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));

        return ResponseEntity.ok(messageService.getMessages(type, username, pageable));
    }

    /**
     * 쪽지 발송 (API)
     */
    @PostMapping("/write")
    @ResponseBody
    public ResponseEntity<String> write(@Valid @RequestBody MessageDto messageDto) {
        String username = getCurrentUsername();
        messageService.sendMessage(messageDto, username);
        return ResponseEntity.ok("success");
    }

    /**
     * 상세 조회 (API)
     */
    @GetMapping("/read")
    @ResponseBody
    public ResponseEntity<MessageDto> read(@RequestParam Long id) {
        String username = getCurrentUsername();
        return ResponseEntity.of(messageService.getMessageDetail(id, username));
    }

    /**
     * 휴지통 이동 (API)
     */
    @PostMapping("/trash")
    @ResponseBody
    public ResponseEntity<Void> moveToTrash(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.moveToTrash(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 복구 하기 (API)
     */
    @PostMapping("/restore")
    @ResponseBody
    public ResponseEntity<Void> restore(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.restoreMessage(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 영구 삭제 (API)
     */
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.permanentDelete(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}