package com.example.community.controller;

import com.example.community.domain.post.Criteria;
import com.example.community.domain.post.Pagination;
import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.ResultDto;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/board/{boardId}/post")
@Slf4j
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 목록
    @GetMapping({"", "/list" })
    public String list(@PathVariable Long boardId, Criteria criteria, Model model) {
        log.info("List boardId = {}, list={}", boardId, criteria);

        Pageable pageable = PageRequest.of(criteria.getPage() - 1,
                criteria.getSize(), Sort.by(Sort.Direction.DESC, "fixed")
                        .and(Sort.by(Sort.Direction.DESC, "id")));

        log.info("pageable = {}", pageable);

        Page<PostDto> postPage = postService.getList(boardId, pageable);

        Pagination pagination = Pagination.of( pageable, postPage.getTotalElements(), postPage.getTotalPages());

        log.info("pagination = {}", pagination);

        model.addAttribute("boardId", boardId);
        model.addAttribute("postPage", postPage);
        model.addAttribute("pagination", pagination);
        model.addAttribute("criteria", criteria);

        return "post/list";
    }

    @PostMapping("/write")
    public String write(@PathVariable Long boardId, PostDto postDto, RedirectAttributes redirectAttributes) {
        log.info("write boardId={}, postDto={}", boardId, postDto);

        Long userId = 1L; // TODO: 로그인 연동 후 제거

        Long createdId = postService.create(boardId, postDto, userId);

        redirectAttributes.addAttribute("boardId", boardId);
        redirectAttributes.addAttribute("id", createdId);
        redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "write"));

        return "redirect:/board/{boardId}/post/read";
    }

    @GetMapping("/write")
    public String write(@PathVariable Long boardId, Model model) {
        model.addAttribute("boardId", boardId);
        model.addAttribute("action", "/board/" + boardId + "/post/write");
        model.addAttribute("title", "Write a new post");
        model.addAttribute("postDto", new PostDto());

        return "post/write";
    }

    @GetMapping("/read")
    public String read(@PathVariable Long boardId, @RequestParam Long id, Criteria criteria, Model model) {
        log.info("read boardId={}, id={}, criteria={}", boardId, id, criteria);

        model.addAttribute("boardId", boardId);
        model.addAttribute("criteria", criteria);
        model.addAttribute("postDto", postService.read(id));

        return "post/read";
    }

    @PostMapping("/update")
    public String update(@PathVariable Long boardId, PostDto postDto, Criteria criteria, RedirectAttributes redirectAttributes) {
        log.info("update boardId={}, postDto={}", boardId, postDto);

        Long userId = 1L; // TODO: 로그인 연동 후 제거

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

    @GetMapping("/delete")
    public String delete(@PathVariable Long boardId, @RequestParam Long id, Criteria criteria, RedirectAttributes redirectAttributes) {
        log.info("delete boardId={}, id={}", boardId, id);

        Long userId = 1L; // TODO: 로그인 연동 후 제거

        if (postService.delete(id, userId)) {
            redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "delete"));
        }

        redirectAttributes.addAttribute("boardId", boardId);
        redirectAttributes.addAttribute("page", criteria.getPage());
        redirectAttributes.addAttribute("size", criteria.getSize());

        return "redirect:/board/{boardId}/post/list";
    }
}