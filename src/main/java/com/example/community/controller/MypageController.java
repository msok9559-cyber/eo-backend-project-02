package com.example.community.controller;

import com.example.community.domain.post.PostDto;
import com.example.community.domain.user.UserDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import com.example.community.service.MypageService;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MypageController {

    private final UserService userService;
    private final MypageService mypageService;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 마이페이지 조회
     * - 로그인한 사용자의 정보를 조회하여 화면에 전달
     * - 비로그인 사용자는 접근 불가
     */
    @GetMapping("")
    public String mypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        log.info("마이페이지 접근 시도");

        if (userDetails == null) {
            // 보안 설정상 비로그인 사용자는 여기까지 못 오는게 정상인데,
            // 혹시 모를 상황 대비로 메시지 표시
            model.addAttribute("error", "로그인이 필요한 서비스입니다");
            return "mypage/mypage";
        }

        String username = userDetails.getUsername();
        log.info("마이페이지 조회 요청: username={}", username);

        UserDto user = userService.read(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        model.addAttribute("user", user);

        Long userId = user.getId();

        // 확인용
        List<PostDto> recent = mypageService.getRecentPosts(userId, 10);

        log.info("recentPosts sample boardId={}, boardTitle={}",
                recent.isEmpty() ? null : recent.get(0).getBoardId(),
                recent.isEmpty() ? null : recent.get(0).getBoardTitle()
        );
        model.addAttribute("recentPosts", recent);

        // 내가 작성한 게시글 최신 10개
        model.addAttribute("recentPosts", mypageService.getRecentPosts(userId, 10));

        // 내가 작성한 댓글 최신 10개
        model.addAttribute("recentComments", mypageService.getRecentComments(userId, 10));

        // 내가 작성한 게시글 개수 표시
        model.addAttribute("myPostsCount",
                postService.getMyPosts(userId, PageRequest.of(0, 1)).getTotalElements());

        // 내가 작성한 댓글 개수 표시
        model.addAttribute("myCommentsCount",
                commentService.getMyComments(userId, PageRequest.of(0, 1)).getTotalElements());

        log.info("마이페이지 조회 성공: username={}", username);

        return "mypage/mypage";
    }

    /**
     * 닉네임 변경
     * - 로그인한 사용자의 닉네임 수정
     * - 닉네임 중복 시 예외 발생
     */
    @PostMapping("/nickname")
    public String updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String nickname,
                                 RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long userId = userDetails.getUser().getId();
        log.info("닉네임 변경 요청: userId={}, newNickname={}", userId, nickname);

        try {
            // 1) DB 업데이트 (여기서 중복이면 예외)
            userService.updateNickname(userId, nickname);

            // 2) DB 업데이트 성공했을 때만 principal 갱신
            refreshPrincipalNickname(nickname);

            log.info("닉네임 변경 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "닉네임이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("닉네임 변경 실패: userId={}, reason={}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage#tab-profile";
    }

    private void refreshPrincipalNickname(String newNickname) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return;

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {

            customUserDetails.getUser().updateNickname(newNickname);

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    authentication.getCredentials(),
                    authentication.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }

    /**
     * 닉네임 중복체크
     */
    @GetMapping("/nickname/check")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> checkNickname(
            @RequestParam String nickname,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인 방어(보통은 여기 안 옴)
        if (userDetails == null) {
            return java.util.Map.of("available", false, "message", "로그인이 필요합니다.");
        }

        // 본인 닉네임이면 사용 가능 처리(UX)
        String currentNick = userDetails.getUser().getNickname();
        if (nickname != null && nickname.equals(currentNick)) {
            return java.util.Map.of("available", true, "message", "현재 닉네임입니다.");
        }

        boolean available = !userService.existsByNickname(nickname);

        return java.util.Map.of(
                "available", available,
                "message", available ? "사용 가능합니다." : "이미 사용 중입니다."
        );
    }

    /**
     * 현재 비밀번호 체크
     */
    @PostMapping("/password/verify")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body
    ) {
        if (userDetails == null) {
            return java.util.Map.of("match", false, "message", "로그인이 필요합니다.");
        }

        String currentPassword = body.getOrDefault("currentPassword", "");
        Long userId = userDetails.getUser().getId();

        try {
            boolean match = userService.verifyCurrentPassword(userId, currentPassword);

            return java.util.Map.of(
                    "match", match,
                    "message", match ? "현재 비밀번호가 일치합니다." : "현재 비밀번호가 일치하지 않습니다."
            );
        } catch (Exception e) {
            return java.util.Map.of("match", false, "message", "비밀번호 확인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 일치 여부 확인
     * - 새 비밀번호 형식 및 확인 일치 검사
     * - 성공 시 암호화 후 저장
     */
    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String newPasswordConfirm,
                                 RedirectAttributes redirectAttributes) {


        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (newPassword == null || newPassword.isBlank()
                || newPasswordConfirm == null || newPasswordConfirm.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호를 입력해주세요.");
            return "redirect:/mypage#tab-password";
        }

        Long userId = userDetails.getUser().getId();
        log.info("비밀번호 변경 요청: userId={}", userId);

        try {
            userService.changePassword(userId, currentPassword, newPassword, newPasswordConfirm);
            log.info("비밀번호 변경 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패: userId={}, reason={}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage";
    }

    /**
     * 내가 작성한 게시글 목록
     * - page, size로 페이징
     * - 최신순 정렬
     * - 게시글 클릭 시 게시글 상세로 이동
     */
    @GetMapping("/posts")
    public String myPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        // page 최소값 방어 (page=0, -1 같은 요청 방지)
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        Long userId = userDetails.getUser().getId();
        log.info("내 게시글 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var postPage = mypageService.getMyPosts(userId, pageable);

        model.addAttribute("postPage", postPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        log.info("내 게시글 목록 조회 성공: userId={}, totalElements={}", userId, postPage.getTotalElements());

        return "mypage/mypage-posts";
    }

    /**
     * 내가 작성한 댓글 목록
     * - page, size로 페이징
     * - 최신순 정렬
     * - 댓글 클릭 시 해당 게시글로 이동
     */
    @GetMapping("/comments")
    public String myComments(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (page < 1) page = 1;
        if (size < 1) size = 10;

        Long userId = userDetails.getUser().getId();
        log.info("내 댓글 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var commentPage = mypageService.getMyComments(userId, pageable);

        model.addAttribute("commentPage", commentPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        log.info("내 댓글 목록 조회 성공: userId={}, totalElements={}", userId, commentPage.getTotalElements());

        return "mypage/mypage-comments";
    }

    /**
     * 회원 탈퇴
     * - 성공 시 사용자 삭제 + 로그아웃 + 홈으로 이동
     */
    @PostMapping("/removeAccount")
    public String removeAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        if (userDetails == null) {
            log.warn("비로그인 사용자의 탈퇴 시도");
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long userId = userDetails.getUser().getId();
        log.info("회원 탈퇴 요청(계정 삭제): userId={}", userId);

        try {
            boolean deleted = userService.delete(userId);

            if (!deleted) {
                log.warn("회원 탈퇴 실패: userId={}, reason=not found", userId);
                redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
                return "redirect:/mypage";
            }

            // 로그아웃 처리
            new SecurityContextLogoutHandler().logout(request, response, null);

            log.info("회원 탈퇴 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            log.error("회원 탈퇴 중 예외 발생: userId={}, reason={}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "회원 탈퇴 처리 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }
}
