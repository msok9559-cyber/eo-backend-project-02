package com.example.community.controller;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.post.ResultDto;
import com.example.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/board")
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시판 전체 목록 조회
     */
    @GetMapping("")
    public String list(Model model) {
        log.info("Accessing Board List...");
        model.addAttribute("boardList", boardService.getList());
        return "board/list";
    }

    /**
     * 게시판 생성 페이지 이동
     */
    @GetMapping("/write")
    public String write(Model model) {
        log.info("Moving to Board Create Form");
        model.addAttribute("boardDto", new BoardDto());
        model.addAttribute("action", "/board/write");
        model.addAttribute("title", "새 게시판 생성");
        return "admin/board-write";
    }

    /**
     * 게시판 생성 처리
     */
    @PostMapping("/write")
    public String write(BoardDto boardDto, RedirectAttributes redirectAttributes) {
        log.info("Creating Board: {}", boardDto);
        boardService.create(boardDto);
        redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "write"));
        return "redirect:/board";
    }

    /**
     * 게시판 수정 페이지 이동
     */
    @GetMapping("/update")
    public String update(@RequestParam Long id, Model model) {
        log.info("Moving to Board Update Form - ID: {}", id);
        boardService.read(id).ifPresent(dto -> model.addAttribute("boardDto", dto));
        model.addAttribute("action", "/board/update");
        model.addAttribute("title", "게시판 이름 수정");
        return "admin/board-write";
    }

    /**
     * 게시판 정보 수정 처리
     */
    @PostMapping("/update")
    public String update(BoardDto boardDto, RedirectAttributes redirectAttributes) {
        log.info("Updating Board: {}", boardDto);
        if (boardService.update(boardDto).isPresent()) {
            redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "update"));
        }
        return "redirect:/board";
    }

    /**
     * 게시판 삭제 처리
     */
    @GetMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting Board - ID: {}", id);
        if (boardService.delete(id)) {
            redirectAttributes.addFlashAttribute("result", ResultDto.of(true, "delete"));
        }
        return "redirect:/board";
    }
}
