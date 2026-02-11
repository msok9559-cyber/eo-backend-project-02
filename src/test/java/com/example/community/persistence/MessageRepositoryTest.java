package com.example.community.persistence;

import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
@Transactional
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity sender;
    private UserEntity receiver;

    @BeforeEach
    void setUp() {
        sender = UserEntity.builder()
                .username("user1")
                .password("1234")
                .nickname("test1")
                .name("홍길동")
                .email("user1@example.com")
                .active(true)
                .build();
        userRepository.save(sender);

        receiver = UserEntity.builder()
                .username("user2")
                .password("1234")
                .nickname("test2")
                .name("김개똥")
                .email("user2@example.com")
                .active(true)
                .build();
        userRepository.save(receiver);
    }

    // 1. 쪽지 생성 및 기본 조회 테스트
    @Test
    @DisplayName("쪽지 저장 및 수신함 조회 테스트")
    void testSaveAndFindReceived() {
        MessageEntity message = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("testTitle")
                .content("testContent")
                .build();

        messageRepository.save(message);

        Page<MessageEntity> result = messageRepository.findByReceiverAndReceiverDeleteState(receiver, 0, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 제목");
        log.info("저장된 쪽지 제목: {}", result.getContent().get(0).getTitle());
    }

    // 2. 휴지통 조회 테스트
    @Test
    @DisplayName("휴지통 메시지 통합 조회 테스트")
    void testFindTrashMessages() {
        // 발신자가 휴지통 보냄
        MessageEntity msg1 = MessageEntity.builder()
                .sender(sender).receiver(receiver).title("발신자삭제").content("내용")
                .build();
        msg1.updateSenderDeleteState(1);
        messageRepository.save(msg1);

        // 수신자가 휴지통 보냄
        MessageEntity msg2 = MessageEntity.builder()
                .sender(receiver).receiver(sender).title("수신자삭제").content("내용")
                .build();
        msg2.updateReceiverDeleteState(1);
        messageRepository.save(msg2);

        // sender 기준 휴지통 조회 (본인이 발신자이면서 삭제했거나, 수신자이면서 삭제한 것 모두 포함)
        Page<MessageEntity> trashPage = messageRepository.findTrashMessages(sender, PageRequest.of(0, 10));

        assertThat(trashPage.getTotalElements()).isEqualTo(2);
        log.info("휴지통 내 메시지 개수: {}", trashPage.getTotalElements());
    }

    // 3. 상태 업데이트 및 물리 삭제 테스트
    @Test
    @DisplayName("읽음 처리 및 영구 삭제 로직 테스트")
    void testStatusAndHardDelete() {
        MessageEntity message = messageRepository.save(
                MessageEntity.builder().sender(sender).receiver(receiver).title("삭제용").content("내용").build()
        );

        // 읽음 처리 확인
        message.markAsRead();
        messageRepository.saveAndFlush(message);
        assertThat(messageRepository.findById(message.getId()).get().getIsRead()).isEqualTo(1);

        // 영구 삭제 조건 테스트 (양측 모두 2일 때)
        message.updateSenderDeleteState(2);
        message.updateReceiverDeleteState(2);

        if (message.getSenderDeleteState() == 2 && message.getReceiverDeleteState() == 2) {
            messageRepository.delete(message);
        }
        messageRepository.flush();

        Optional<MessageEntity> deleted = messageRepository.findById(message.getId());
        assertThat(deleted).isEmpty();
        log.info("물리 삭제 조건 충족 및 삭제 완료 확인");
    }
}