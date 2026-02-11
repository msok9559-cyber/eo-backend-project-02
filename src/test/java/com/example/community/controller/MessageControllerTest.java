package com.example.community.controller;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.MessageRepository;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.MessageService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

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

        // 시큐리티 인증 세팅 (sender로 로그인한 상태 가정)
        CustomUserDetails userDetails = new CustomUserDetails(sender);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("received - 받은 쪽지함 데이터 로드 확인")
    void testReadReceivedPage() throws Exception {
        mockMvc.perform(get(MESSAGES_URI + "/received")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("write - 쪽지 발송 성공 테스트")
    void testWrite() throws Exception {
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        String json = objectMapper.writeValueAsString(messageDto);

        mockMvc.perform(post(MESSAGES_URI + "/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    /**
     * 쪽지 삭제(휴지통 이동) 테스트
     * 실제 데이터를 생성한 후 삭제를 진행하여 RuntimeException 방지
     */
    @Test
    @DisplayName("trash - 휴지통 이동 성공 테스트")
    void testMoveToTrash() throws Exception {
        log.info("=== testMoveToTrash 시작 ===");

        // 1. 쪽지를 하나 발송하여 DB에 저장
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("삭제될 쪽지")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        // 2. 저장된 쪽지의 실제 ID 조회
        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        // 3. 생성된 ID로 삭제 요청
        mockMvc.perform(post(MESSAGES_URI + "/trash")
                        .param("id", targetId.toString())
                        .param("userType", "sender"))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("=== testMoveToTrash 완료 ===");
    }
}