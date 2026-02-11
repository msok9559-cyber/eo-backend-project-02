package com.example.community.controller;

import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser;
    private UserEntity testAdmin;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = UserEntity.builder()
                .username("testuser")
                .password(passwordEncoder.encode("1234"))
                .name("테스트 유저")
                .nickname("유저")
                .email("user@test.com")
                .role(UserRole.USER)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(testUser);

        // 테스트 관리자 생성
        testAdmin = UserEntity.builder()
                .username("testadmin")
                .password(passwordEncoder.encode("1234"))
                .name("테스트 관리자")
                .nickname("관리자")
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("사용자 목록 조회 - ADMIN")
    void testGetUsers() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("page", "0")
                        .param("size", "20")
                        .param("role", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andDo(print());
    }

//    //페이지 만들고 다시 테스트
//    @Test
//    @DisplayName("사용자 목록 조회 - USER 권한 (실패)")
//    void testGetUsersWithUser() throws Exception {
//        mockMvc.perform(get("/admin/users")
//                        .with(user("testuser").roles("USER")))
//                .andExpect(status().isForbidden())
//                .andDo(print());
//    }

    @Test
    @DisplayName("사용자 목록 조회 - 역할 필터")
    void testGetUsersByRole() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 목록 조회 - 키워드 검색")
    void testGetUsersWithKeyword() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("keyword", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 상세 조회")
    void testGetUserDetail() throws Exception {
        mockMvc.perform(get("/admin/users/" + testUser.getId())
                        .with(user("testadmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트 유저"))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 권한 변경 성공")
    void testChangeUserRole() throws Exception {
        mockMvc.perform(patch("/admin/users/" + testUser.getId() + "/role")
                        .with(user(testAdmin.getUsername()).roles("ADMIN"))
                        .param("role", "ADMIN")
                        .param("reason", "테스트 승급"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("권한이 변경되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 정지 성공")
    void testBanUser() throws Exception {
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/ban")
                        .with(user(testAdmin.getUsername()).roles("ADMIN"))
                        .param("reason", "테스트 정지")
                        .param("duration", "7")
                        .param("note", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자가 정지되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 활성화")
    void testActivateUser() throws Exception {
        // given - 먼저 비활성화
        testUser.setActive(false);
        userRepository.save(testUser);

        // when & then
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/activate")
                        .with(user(testAdmin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자가 활성화되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 비활성화")
    void testDeactivateUser() throws Exception {
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/deactivate")
                        .with(user(testAdmin.getUsername()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자가 비활성화되었습니다."))
                .andDo(print());
    }
    @Test
    @DisplayName("게시판 목록 조회 - ADMIN")
    void testGetBoards() throws Exception {
        mockMvc.perform(get("/admin/boards")
                        .with(user("testadmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("게시판 생성 성공")
    void testCreateBoard() throws Exception {
        mockMvc.perform(post("/admin/boards")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("title", "테스트게시판"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시판이 생성 되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시판 생성 실패 - 중복 제목")
    void testCreateBoardDuplicate() throws Exception {
        // given - 먼저 게시판 생성
        mockMvc.perform(post("/admin/boards")
                .with(user("testadmin").roles("ADMIN"))
                .param("title", "중복게시판"));

        // when & then - 같은 제목으로 다시 생성 시도
        mockMvc.perform(post("/admin/boards")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("title", "중복게시판"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("게시판 수정 성공")
    void testUpdateBoard() throws Exception {
        // given - 먼저 게시판 생성
        MvcResult createResult = mockMvc.perform(post("/admin/boards")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("title", "수정전게시판"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long boardId = objectMapper.readTree(responseBody).get("boardId").asLong();

        // when & then - 게시판 수정
        mockMvc.perform(put("/admin/boards/" + boardId)
                        .with(user("testadmin").roles("ADMIN"))
                        .param("title", "수정후게시판"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시판이 수정되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시판 삭제 성공")
    void testDeleteBoard() throws Exception {
        // given - 먼저 게시판 생성
        MvcResult createResult = mockMvc.perform(post("/admin/boards")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("title", "삭제할게시판"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long boardId = objectMapper.readTree(responseBody).get("boardId").asLong();

        // when & then - 게시판 삭제
        mockMvc.perform(delete("/admin/boards/" + boardId)
                        .with(user("testadmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시판이 삭제되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("전체 게시글 조회 - ADMIN")
    void testGetPosts() throws Exception {
        mockMvc.perform(get("/admin/posts")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시판별 게시글 조회")
    void testGetPostsByBoard() throws Exception {
        mockMvc.perform(get("/admin/posts")
                        .with(user("testadmin").roles("ADMIN"))
                        .param("boardId", "1"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("전체 댓글 조회 - ADMIN")
    void testGetComments() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .with(user("testadmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andDo(print());
    }
}

