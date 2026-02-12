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
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    /**
     * 현재 로그인한 사용자의 ID를 가져오는 공통 메서드
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 전체/받은/보낸/휴지통 쪽지함 화면 조회
     */
    @GetMapping({"/all", "/received", "/sent", "/trash"})
    public String listPage(@RequestParam(defaultValue = "1") int page,
                           Model model,
                           jakarta.servlet.http.HttpServletRequest request) {

        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));

        String uri = request.getRequestURI();
        String type = uri.substring(uri.lastIndexOf("/") + 1);
        if ("messages".equals(type)) type = "all";

        Page<MessageDto> messagePage = messageService.getMessages(type, username, pageable);

        model.addAttribute("messages", messagePage);
        model.addAttribute("currentType", type);
        return "message/message";
    }

    /**
     * 작성 화면 반환
     */
    @GetMapping("/write")
    public String writePage() {
        return "message/message-write";
    }

    /**
     * 상세 조회 화면 반환
     */
    @GetMapping("/read")
    public String readPage(@RequestParam Long id,
                           @RequestParam(defaultValue = "all") String type,
                           Model model) {
        model.addAttribute("currentType", type);
        return "message/message-read";
    }

    /* --- API (JSON Response) --- */

    /**
     * 상세 정보 조회 API
     */
    @GetMapping("/api/read")
    @ResponseBody
    public ResponseEntity<MessageDto> getMessageDetailApi(@RequestParam Long id) {
        String username = getCurrentUsername();
        return messageService.getMessageDetail(id, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 쪽지 발송 (API)
     */
    @PostMapping("/api/write")
    @ResponseBody
    public ResponseEntity<String> write(@Valid @RequestBody MessageDto messageDto) {
        String username = getCurrentUsername();
        messageService.sendMessage(messageDto, username);
        return ResponseEntity.ok("success");
    }

    /**
     * 휴지통 이동 (API)
     */
    @PostMapping("/api/trash")
    @ResponseBody
    public ResponseEntity<Void> moveToTrash(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.moveToTrash(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 복구 하기 (API)
     */
    @PostMapping("/api/restore")
    @ResponseBody
    public ResponseEntity<Void> restore(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.restoreMessage(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 영구 삭제 (API)
     */
    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.permanentDelete(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 선택 삭제 - 휴지통 이동 (Bulk API)
     */
    @PostMapping("/api/trash/bulk")
    @ResponseBody
    public ResponseEntity<Void> moveToTrashBulk(@RequestBody Map<String, List<Long>> request) {
        String username = getCurrentUsername();
        List<Long> ids = request.get("ids");
        if (ids != null) {
            // 대량 삭제 시에도 서비스 로직 호출
            ids.forEach(id -> messageService.moveToTrash(id, username, "received"));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 선택 삭제 - 영구 삭제 (Bulk API)
     */
    @PostMapping("/api/delete/bulk")
    @ResponseBody
    public ResponseEntity<Void> deleteBulk(@RequestBody Map<String, List<Long>> request) {
        String username = getCurrentUsername();
        List<Long> ids = request.get("ids");
        if (ids != null) {
            // 대량 영구 삭제 시에도 서비스 로직 호출
            ids.forEach(id -> messageService.permanentDelete(id, username, "received"));
        }
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