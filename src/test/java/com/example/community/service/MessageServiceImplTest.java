package com.example.community.service;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
@Transactional
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    private UserEntity sender;
    private UserEntity receiver;

    @BeforeEach
    void setUp() {
        sender = UserEntity.builder()
                .username("sender1")
                .password("1234")
                .nickname("보낸사람")
                .name("발신자이름")
                .email("sender@test.com")
                .active(true)
                .build();
        userRepository.save(sender);

        receiver = UserEntity.builder()
                .username("receiver1")
                .password("1234")
                .nickname("받는사람")
                .name("수신자이름")
                .email("receiver@test.com")
                .active(true)
                .build();
        userRepository.save(receiver);
    }

    @Test
    @DisplayName("쪽지 발송 및 타입별 목록 조회 테스트")
    void sendMessageAndListTest() {
        // 1. 발송
        MessageDto dto = MessageDto.builder()
                .receiverUsername(receiver.getUsername())
                .title("안녕하세요")
                .content("테스트 내용입니다.")
                .build();
        messageService.sendMessage(dto, sender.getUsername());

        // 2. 수신함 조회
        Page<MessageDto> received = messageService
                .getMessages("수신", receiver.getUsername(), PageRequest.of(0, 10));

        assertThat(received.getContent()).isNotEmpty();
        assertThat(received.getContent().get(0).getSenderUsername()).isEqualTo(sender.getUsername());

        // 3. 발신함 조회
        Page<MessageDto> sent = messageService.getMessages("발신", sender.getUsername(), PageRequest.of(0, 10));
        assertThat(sent.getContent()).isNotEmpty();
        log.info("발신함/수신함 조회 성공");
    }

    @Test
    @DisplayName("쪽지 휴지통 이동 및 복구 테스트")
    void messageTrashRestoreTest() {
        // 1. 초기 쪽지 생성
        MessageDto dto = MessageDto.builder()
                .receiverUsername(receiver.getUsername()).title("삭제/복구테스트").content("내용")
                .build();
        messageService.sendMessage(dto, sender.getUsername());
        Long msgId = messageService.getMessages("발신", sender.getUsername(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        // 2. 휴지통 이동 (Soft Delete)
        messageService.moveToTrash(msgId, sender.getUsername(), "발신");

        assertThat(messageService.getMessages("발신", sender.getUsername(), PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThat(messageService.getMessages("휴지통", sender.getUsername(), PageRequest.of(0, 10)).getContent()).isNotEmpty();
        log.info("휴지통 이동 확인 완료");

        // 3. 복구 처리
        messageService.restoreMessage(msgId, sender.getUsername(), "발신");
        assertThat(messageService.getMessages("발신", sender.getUsername(), PageRequest.of(0, 10)).getContent()).isNotEmpty();
        assertThat(messageService.getMessages("휴지통", sender.getUsername(), PageRequest.of(0, 10)).getContent()).isEmpty();
        log.info("메시지 복구 확인 완료");
    }

    @Test
    @DisplayName("상세 보기 시 읽음 처리 자동 검증")
    void messageReadDetailTest() {
        MessageDto dto = MessageDto.builder()
                .receiverUsername(receiver.getUsername()).title("읽음테스트").content("내용")
                .build();
        messageService.sendMessage(dto, sender.getUsername());
        Long msgId = messageService.getMessages("수신", receiver.getUsername(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        // 수신자가 상세 페이지 접근
        messageService.getMessageDetail(msgId, receiver.getUsername());

        // 다시 조회하여 isRead가 1인지 확인
        MessageDto result = messageService.getMessageDetail(msgId, receiver.getUsername()).get();
        assertThat(result.getIsRead()).isEqualTo(1);
        assertThat(result.getReadedAt()).isNotNull();
        log.info("읽음 처리 시간: {}", result.getReadedAt());
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 발송 시 예외 발생")
    void sendToNonExistUser() {
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername("no_user")
                .title("가짜")
                .content("내용")
                .build();

        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(requestDto, sender.getUsername());
        });
        log.info("예외 처리 정상 작동 확인");
    }
}