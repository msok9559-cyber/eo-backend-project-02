package com.example.community.controller;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Long postId;
    private final String COMMENTS_URI = "/api/posts/%d/comments";

    @BeforeEach
    void setUp() {
        // ===== 테스트 유저 생성 =====
        UserEntity user = userRepository.findByUsername("commentUser")
                .orElseGet(() ->
                        userRepository.save(
                                UserEntity.builder()
                                        .username("commentUser")
                                        .password("password123!")
                                        .name("commentUser")
                                        .nickname("tester")
                                        .email("comment@test.com")
                                        .active(true)
                                        .build()
                        )
                );

        // ===== SecurityContext에 인증 사용자 세팅 =====
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // ===== 게시글 생성 =====
        PostEntity post = postRepository.save(
                PostEntity.builder()
                        .boardId(1L)
                        .userId(user.getId())
                        .title("test_post")
                        .content("post_content")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );
        postId = post.getId();
    }

    @Test
    public void testCreate() throws Exception {
        CommentDto dto = CommentDto.builder()
                .content("comment_content")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(String.format(COMMENTS_URI, postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("comment_content"))
                .andDo(print());
    }


    @Test
    void testCreateFail() throws Exception {
        CommentDto dto = CommentDto.builder().build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(String.format(COMMENTS_URI, postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testUpdate() throws Exception {
        // 댓글 생성
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post(String.format(COMMENTS_URI, postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        CommentDto.builder().content("initial").build()
                                ))
                )
                .andReturn();

        Long commentId = new JSONObject(result.getResponse().getContentAsString())
                .getLong("id");

        CommentDto updateDto = CommentDto.builder()
                .id(commentId)
                .content("update_content")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.put(
                                        String.format(COMMENTS_URI, postId) + "/" + commentId
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정 후"))
                .andDo(print());
    }

    @Test
    void testDeleteFail() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                String.format(COMMENTS_URI, postId) + "/9999"
                        )
                )
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
