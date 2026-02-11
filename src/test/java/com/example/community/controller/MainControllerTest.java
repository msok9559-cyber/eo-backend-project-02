package com.example.community.controller;

import com.example.community.domain.board.BoardEntity;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.BoardRepository;
import com.example.community.persistence.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MainController 통합 테스트
 * - @SpringBootTest: 전체 스프링 컨텍스트 로딩
 * - H2(테스트 DB)에 데이터를 넣고 실제 서비스/레포지토리까지 포함해 검증
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Slf4j
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    PostRepository postRepository;

    // 테스트 실행 전 데이터 초기화
    @BeforeEach
    void setUp() {
        log.info("=== 데이터 초기화 시작 ===");

        postRepository.deleteAll();
        boardRepository.deleteAll();

        // 공지 카테고리 게시판 생성
        BoardEntity noticeBoard = boardRepository.save(
                BoardEntity.builder().title("커뮤니티 공지").build()
        );

        // 일반 게시판 생성
        BoardEntity freeBoard = boardRepository.save(
                BoardEntity.builder().title("자유게시판").build()
        );

        // 공지 게시글 생성
        postRepository.saveAll(List.of(
                PostEntity.builder()
                        .userId(1L)
                        .boardId(noticeBoard.getId())
                        .title("공지글1")
                        .content("공지 내용1")
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build(),
                PostEntity.builder()
                        .userId(1L)
                        .boardId(noticeBoard.getId())
                        .title("공지글2")
                        .content("공지 내용2")
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        ));

        // 일반 게시글 생성 (검색 테스트용)
        postRepository.saveAll(List.of(
                PostEntity.builder()
                        .userId(1L)
                        .boardId(freeBoard.getId())
                        .title("테스트 제목 검색키워드")
                        .content("일반 내용")
                        .viewCount(100)
                        .commentsCount(0)
                        .likesCount(0)
                        .build(),
                PostEntity.builder()
                        .userId(1L)
                        .boardId(freeBoard.getId())
                        .title("일반 제목")
                        .content("검색키워드 내용입니다")
                        .viewCount(50)
                        .commentsCount(0)
                        .likesCount(0)
                        .build(),
                PostEntity.builder()
                        .userId(1L)
                        .boardId(freeBoard.getId())
                        .title("인기글")
                        .content("조회수 높은 글")
                        .viewCount(1000)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        ));

        log.info("=== 데이터 초기화 완료 ===");
    }

    /**
     * 기본 메인 페이지 로드 테스트
     */
    @Test
    @DisplayName("index - 메인 페이지 기본 로드 및 모델 속성 확인")
    void testIndex() throws Exception {
        log.info("=== testIndex 시작 ===");

        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                // 공지 카테고리 1개, 일반 게시판 1개
                .andExpect(model().attribute("noticeBoardList", hasSize(1)))
                .andExpect(model().attribute("boardList", hasSize(1)))
                // 공지글 2개가 noticeList로 들어와야 함
                .andExpect(model().attribute("noticeList", hasSize(2)))
                // 게시글 페이지 존재
                .andExpect(model().attributeExists("postPage"))
                // 인기 게시글 존재
                .andExpect(model().attributeExists("popularPosts"))
                // 검색 파라미터
                .andExpect(model().attribute("searchType", ""))
                .andExpect(model().attribute("keyword", ""));

        log.info("=== testIndex 완료 ===");
    }

    /**
     * 공지 카테고리가 없을 경우 테스트
     */
    @Test
    @DisplayName("index - 공지 카테고리 없을 때 noticeList 빈 배열")
    void testIndex_whenNoNoticeBoards() throws Exception {
        log.info("=== testIndex_whenNoNoticeBoards 시작 ===");

        postRepository.deleteAll();
        boardRepository.deleteAll();

        boardRepository.save(BoardEntity.builder().title("자유게시판").build());

        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("noticeBoardList", hasSize(0)))
                .andExpect(model().attribute("boardList", hasSize(1)))
                .andExpect(model().attribute("noticeList", hasSize(0)));

        log.info("=== testIndex_whenNoNoticeBoards 완료 ===");
    }

    /**
     * 제목 검색 테스트
     */
    @Test
    @DisplayName("searchByTitle - 제목으로 검색")
    void testSearchByTitle() throws Exception {
        log.info("=== testSearchByTitle 시작 ===");

        mockMvc.perform(get("/")
                        .param("searchType", "title")
                        .param("keyword", "검색키워드"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchType", "title"))
                .andExpect(model().attribute("keyword", "검색키워드"))
                .andExpect(model().attributeExists("postPage"));

        log.info("=== testSearchByTitle 완료 ===");
    }

    /**
     * 내용 검색 테스트
     */
    @Test
    @DisplayName("searchByContent - 내용으로 검색")
    void testSearchByContent() throws Exception {
        log.info("=== testSearchByContent 시작 ===");

        mockMvc.perform(get("/")
                        .param("searchType", "content")
                        .param("keyword", "검색키워드"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchType", "content"))
                .andExpect(model().attribute("keyword", "검색키워드"));

        log.info("=== testSearchByContent 완료 ===");
    }

    /**
     * 제목+내용 검색 테스트
     */
    @Test
    @DisplayName("searchByTitleContent - 제목+내용 검색")
    void testSearchByTitleContent() throws Exception {
        log.info("=== testSearchByTitleContent 시작 ===");

        mockMvc.perform(get("/")
                        .param("searchType", "titleContent")
                        .param("keyword", "검색키워드"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchType", "titleContent"))
                .andExpect(model().attribute("keyword", "검색키워드"));

        log.info("=== testSearchByTitleContent 완료 ===");
    }

    /**
     * 빈 키워드 검색 테스트
     */
    @Test
    @DisplayName("searchWithEmptyKeyword - 빈 키워드는 전체 목록 반환")
    void testSearchWithEmptyKeyword() throws Exception {
        log.info("=== testSearchWithEmptyKeyword 시작 ===");

        mockMvc.perform(get("/")
                        .param("searchType", "title")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attributeExists("postPage"));

        log.info("=== testSearchWithEmptyKeyword 완료 ===");
    }

    /**
     * 페이징 테스트
     */
    @Test
    @DisplayName("paging - 페이징 파라미터 정상 처리")
    void testPaging() throws Exception {
        log.info("=== testPaging 시작 ===");

        mockMvc.perform(get("/")
                        .param("page", "2")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("postPage"));

        log.info("=== testPaging 완료 ===");
    }

    /**
     * 인기 게시글 조회 테스트
     */
    @Test
    @DisplayName("popularPosts - 인기 게시글 모델에 포함 확인")
    void testPopularPosts() throws Exception {
        log.info("=== testPopularPosts 시작 ===");

        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("popularPosts"));

        log.info("=== testPopularPosts 완료 ===");
    }

    /**
     * 에러 페이지 테스트
     */
    @Test
    @DisplayName("error - 에러 페이지 로드 확인")
    void testError() throws Exception {
        log.info("=== testError 시작 ===");

        mockMvc.perform(get("/error"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        log.info("=== testError 완료 ===");
    }
}