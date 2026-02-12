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
    private final String API_URI = "/messages/api";

    @BeforeEach
    void setUp() {
        // 발신자(나) 생성
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

        // 수신자(상대방) 생성
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

        // Security Context에 발신자 정보 주입
        CustomUserDetails userDetails = new CustomUserDetails(sender);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("전체/받은/보낸 쪽지함 페이지 뷰 반환 테스트")
    void testListPages() throws Exception {
        // /messages/all, /received, /sent 경로 테스트
        mockMvc.perform(get(MESSAGES_URI + "/received"))
                .andExpect(status().isOk())
                .andExpect(view().name("message/message"))
                .andExpect(model().attributeExists("messages"))
                .andExpect(model().attribute("currentType", "received"));
    }

    @Test
    @DisplayName("api/write - 닉네임 기반 쪽지 발송 성공 테스트")
    void testWriteByNickname() throws Exception {
        // 중요: receiverUsername 대신 receiverNickname을 사용해야 함
        MessageDto messageDto = MessageDto.builder()
                .receiverNickname("받는사람")
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        String json = objectMapper.writeValueAsString(messageDto);

        mockMvc.perform(post(API_URI + "/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    @DisplayName("api/read - 쪽지 상세 데이터(JSON) 조회 테스트")
    void testGetMessageDetailApi() throws Exception {
        // 테스트용 쪽지 생성 (서비스 로직 활용)
        MessageDto messageDto = MessageDto.builder()
                .receiverNickname("받는사람")
                .title("상세조회용")
                .content("상세내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        mockMvc.perform(get(API_URI + "/read")
                        .param("id", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("상세조회용"))
                .andExpect(jsonPath("$.content").value("상세내용"));
    }

    @Test
    @DisplayName("api/trash - 휴지통 이동 테스트")
    void testMoveToTrash() throws Exception {
        MessageDto messageDto = MessageDto.builder()
                .receiverNickname("받는사람")
                .title("삭제될 쪽지")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        mockMvc.perform(post(API_URI + "/trash")
                        .param("id", targetId.toString())
                        .param("userType", "sent")) // 내가 보낸 쪽지함에서 삭제
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("api/restore - 쪽지 복구 테스트")
    void testRestore() throws Exception {
        MessageDto messageDto = MessageDto.builder()
                .receiverNickname("받는사람")
                .title("복구 테스트")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        mockMvc.perform(post(API_URI + "/restore")
                        .param("id", targetId.toString())
                        .param("userType", "sent"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("api/delete - 영구 삭제 테스트")
    void testPermanentDelete() throws Exception {
        MessageDto messageDto = MessageDto.builder()
                .receiverNickname("받는사람")
                .title("영구삭제 테스트")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        mockMvc.perform(post(API_URI + "/delete")
                        .param("id", targetId.toString())
                        .param("userType", "sent"))
                .andExpect(status().isOk());
    }
}