package com.example.community.controller;

import com.example.community.domain.Criteria;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/board/")
@Slf4j
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping({"", "/list" })
    public String list(@RequestParam("boardId") Long boardId, Criteria criteria, Model model) {
        log.info("List boardId = {}, criteria={}", boardId,criteria);

        Pageable pagealbe = PageRequest.of(criteria.getPage() - 1,
                criteria.getSize(), Sort.by(Sort.Direction.DESC, "fixed")
                        .and(Sort.by(Sort.Direction.DESC, "id")));
    }
}
