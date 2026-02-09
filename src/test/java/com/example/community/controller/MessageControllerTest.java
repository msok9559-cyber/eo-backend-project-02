package com.example.community.controller;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Transactional
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final String MESSAGES_URI = "/messages";

    @BeforeEach
    void setUp() {
        // 테스트용 발신자 저장
        UserEntity sender = userRepository.findByUsername("sender").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .username("sender")
                        .password("password123!")
                        .name("발신자")
                        .nickname("보내는사람")
                        .email("sender@test.com")
                        .active(true)
                        .build())
        );

        // 테스트용 수신자 저장
        userRepository.findByUsername("receiver").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .username("receiver")
                        .password("password456!")
                        .name("수신자")
                        .nickname("받는사람")
                        .email("receiver@test.com")
                        .active(true)
                        .build())
        );

        CustomUserDetails userDetails = new CustomUserDetails(sender);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("쪽지 발송 테스트")
    public void testCreate() throws Exception {
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        String json = objectMapper.writeValueAsString(messageDto);

        mockMvc.perform(MockMvcRequestBuilders.post(MESSAGES_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("Message creation test completed");
    }

    @Test
    @DisplayName("받은 쪽지함 조회 테스트")
    public void testReadReceived() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(MESSAGES_URI + "/received")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andDo(print());

        log.info("Read received messages test completed");
    }

    @Test
    @DisplayName("쪽지 발송 실패 테스트 (제목 누락)")
    public void testCreateFail() throws Exception {
        // 제목(title)이 없는 DTO
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .content("테스트 제목이 없어서 실패")
                .build();

        String json = objectMapper.writeValueAsString(messageDto);

        mockMvc.perform(MockMvcRequestBuilders.post(MESSAGES_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        log.info("Message creation fail test completed");
    }

    @Test
    @DisplayName("쪽지 삭제 테스트")
    public void testDelete() throws Exception {
        // 임의의 ID 1번 삭제 요청
        mockMvc.perform(MockMvcRequestBuilders.delete(MESSAGES_URI + "/1"))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("Message delete test completed");
    }
}