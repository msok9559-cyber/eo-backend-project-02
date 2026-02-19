package com.example.community.controller;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.post.Criteria;
import com.example.community.domain.post.Pagination;
import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.ResultDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.BoardService;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/board/{boardId}/post")
@Slf4j
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final BoardService boardService;

    // 게시글 목록
    @GetMapping({"", "/list" })
    public String list(@PathVariable Long boardId, Criteria criteria, Model model) {
        log.info("List boardId = {}, list={}", boardId, criteria);

        // 1) 전체 게시판 조회
        List<BoardDto> allBoards = boardService.getList();

        // 2) 공지 카테고리 / 일반 게시판 분리
        List<BoardDto> noticeBoardList = allBoards.stream()
                .filter(b -> "NOTICE".equals(b.getCategory()))
                .toList();

        List<BoardDto> boardList = allBoards.stream()
                .filter(b -> b.getCategory() == null || !"NOTICE".equals(b.getCategory()))
                .toList();

        model.addAttribute("noticeBoardList", noticeBoardList);
        model.addAttribute("boardList", boardList);

        // 3) 메인 가운데 공지 영역 (최신 5개)
        if (!noticeBoardList.isEmpty()) {
            Long noticeBoardId = noticeBoardList.get(0).getId();
            Pageable noticePageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
            var noticePage = postService.getList(noticeBoardId, noticePageable);
            model.addAttribute("noticeList", noticePage.getContent());
        } else {
            model.addAttribute("noticeList", List.of());
        }

        Pageable pageable = PageRequest.of(criteria.getPage() - 1,
                criteria.getSize(), Sort.by(Sort.Direction.DESC, "fixed")
                        .and(Sort.by(Sort.Direction.DESC, "id")));

        log.info("pageable = {}", pageable);

        Page<PostDto> postPage;
        // 키워드가 있는지 확인
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            postPage = postService.searchPostsInBoard(boardId, criteria.getSearchType(), criteria.getKeyword(), pageable);
        } else {
            postPage = postService.getList(boardId, pageable);
        }

        Pagination pagination = Pagination.of( pageable, postPage.getTotalElements(), postPage.getTotalPages());

        log.info("pagination = {}", pagination);

        model.addAttribute("boardId", boardId);
        model.addAttribute("postPage", postPage);
        model.addAttribute("pagination", pagination);
        model.addAttribute("criteria", criteria);

        // 5) 인기 게시글 TOP 10 (오른쪽 사이드바용)
        Pageable popularPageable = PageRequest.of(0, 10);
        Page<PostDto> popularPosts = postService.getPopularPosts(popularPageable);
        model.addAttribute("popularPosts", popularPosts.getContent());

        return "post/list";
    }

    @PostMapping("/write")
    public String write(@PathVariable Long boardId, PostDto postDto,
                        RedirectAttributes redirectAttributes,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("write boardId={}, postDto={}", boardId, postDto);

        Long userId = userDetails.getId();
        Long createdId = postService.create(boardId, postDto, userId);

        redirectAttributes.addAttribute("boardId", boardId);
        redirectAttributes.addAttribute("id", createdId);
        redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "write"));

        return "redirect:/board/{boardId}/post/read";
    }

    @GetMapping("/write")
    public String write(@PathVariable Long boardId, Model model) {
        log.info("writeForm boardId={}", boardId);

        model.addAttribute("boardId", boardId);
        model.addAttribute("action", "/board/" + boardId + "/post/write");
        model.addAttribute("title", "Write a new post");
        model.addAttribute("postDto", new PostDto());

        // 게시판 목록 추가
        List<BoardDto> allBoards = boardService.getList();
        model.addAttribute("noticeBoardList", allBoards.stream()
                .filter(b -> "NOTICE".equals(b.getCategory()))
                .toList());
        model.addAttribute("boardList", allBoards.stream()
                .filter(b -> b.getCategory() == null || !"NOTICE".equals(b.getCategory()))
                .toList());

        // 인기 게시물 추가 (오른쪽 사이드바용)
        Pageable popularPageable = PageRequest.of(0, 10);
        Page<PostDto> popularPosts = postService.getPopularPosts(popularPageable);
        model.addAttribute("popularPosts", popularPosts.getContent());

        return "post/write";
    }

    @GetMapping("/read")
    public String read(@PathVariable Long boardId,
                       @RequestParam Long id,
                       Criteria criteria,
                       Model model,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("read boardId={}, id={}, criteria={}", boardId, id, criteria);

        model.addAttribute("boardId", boardId);
        model.addAttribute("criteria", criteria);
        model.addAttribute("postDto", postService.read(id));

        // 현재 사용자 ID와 관리자 여부를 Model에 추가 (댓글 기능용)
        Long currentUserId = null;
        boolean isAdmin = false;
        if (userDetails != null) {
            currentUserId = userDetails.getUser().getId();
            isAdmin = userDetails.getUser().getRole().toString().equals("ADMIN");
        }
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isAdmin", isAdmin);

        // 인기 게시글 데이터 조회
        Pageable popularPageable = PageRequest.of(0, 10);
        model.addAttribute("popularPosts", postService.getPopularPosts(popularPageable).getContent());

        // 게시판 카테고리 목록 조회
        List<BoardDto> allBoards = boardService.getList();

        // 카테고리가 'NOTICE'인 게시판만 필터링
        model.addAttribute("noticeBoardList", boardService
                .getList().stream().filter(b -> "NOTICE".equals(b.getCategory())).toList());

        // 카테고리가 없거나 'NOTICE'가 아닌 일반 게시판만 필터링
        model.addAttribute("boardList", boardService
                .getList().stream().filter(b -> b.getCategory() == null || !"NOTICE".equals(b.getCategory())).toList());

        return "post/read";
    }

    @PostMapping("/update")
    public String update(@PathVariable Long boardId, PostDto postDto, Criteria criteria,
                         RedirectAttributes redirectAttributes,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("update boardId={}, postDto={}", boardId, postDto);

        Long userId = userDetails.getId();

        if (postService.update(postDto, userId)) {
            redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "update"));
        }

        redirectAttributes.addAttribute("boardId", boardId);
        redirectAttributes.addAttribute("id", postDto.getId());
        redirectAttributes.addAttribute("page", criteria.getPage());
        redirectAttributes.addAttribute("size", criteria.getSize());

        return "redirect:/board/{boardId}/post/read";
    }

    @GetMapping("/update")
    public String update(@PathVariable Long boardId,  @RequestParam Long id, Criteria criteria, Model model){
        log.info("updateForm boardId={}, id={}", boardId, id);

        model.addAttribute("boardId", boardId);
        model.addAttribute("criteria", criteria);
        model.addAttribute("postDto", postService.read(id));
        model.addAttribute("action", "/board/" + boardId + "/post/update");
        model.addAttribute("title", "Update the post");

        return "post/write";
    }

    @PostMapping("/delete")
    public String delete(@PathVariable Long boardId, @RequestParam Long id, Criteria criteria,
                         RedirectAttributes redirectAttributes,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("delete boardId={}, id={}", boardId, id);

        Long userId = userDetails.getId();

        if (postService.delete(id, userId)) {
            redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "delete"));
        }

        // 직접 URL 작성
        redirectAttributes.addAttribute("boardId", boardId);
        if (criteria.getPage() > 0) {
            redirectAttributes.addAttribute("page", criteria.getPage());
        }
        if (criteria.getSize() > 0) {
            redirectAttributes.addAttribute("size", criteria.getSize());
        }
        if (criteria.getSearchType() != null && !criteria.getSearchType().isEmpty()) {
            redirectAttributes.addAttribute("searchType", criteria.getSearchType());
            redirectAttributes.addAttribute("keyword", criteria.getKeyword());
        }

        return "redirect:/board/{boardId}/post/list";
    }
}