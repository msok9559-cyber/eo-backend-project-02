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
import org.springframework.data.domain.Sort;
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
                .build();
        userRepository.save(sender);

        receiver = UserEntity.builder()
                .username("user2")
                .password("1234")
                .nickname("test2")
                .name("김개똥")
                .email("user2@example.com")
                .build();
        userRepository.save(receiver);
    }

    // 1. 기본 저장 및 조회

    @Test
    @DisplayName("쪽지 발송 및 상세 필드 데이터 검증")
    public void testSendMessageWithExistingUsers() {
        String testTitle = "[TEST] 삭제된 제목";
        String testContent = "[TEST] 삭제된 내용";

        MessageEntity message = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title(testTitle)
                .content(testContent)
                .build();

        messageRepository.save(message);

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<MessageEntity> result = messageRepository
                .findByReceiverAndReceiverDeleteState(receiver, 0, pageable);

        assertThat(result.getTotalElements()).isGreaterThan(0);

        MessageEntity foundMessage = result.getContent().get(0);
        assertThat(foundMessage.getTitle()).isEqualTo(testTitle);
        assertThat(foundMessage.getContent()).isEqualTo(testContent);
        assertThat(foundMessage.getSender().getNickname()).isEqualTo("test1");
        assertThat(foundMessage.getIsRead()).isEqualTo(0);

        log.info("검증 완료 - 받은 쪽지 제목: {}", foundMessage.getTitle());
    }

    @Test
    @DisplayName("상세 조회 테스트")
    void testFindById() {
        MessageEntity message = messageRepository.save(
                MessageEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .title("[TEST]상세조회")
                        .content("[TEST]내용")
                        .build()
        );

        Optional<MessageEntity> optionalMessage = messageRepository.findById(message.getId());

        assertThat(optionalMessage).isPresent();
        assertThat(optionalMessage.get().getTitle()).isEqualTo("상세조회");
        log.info("ID 상세 조회 성공");
    }

    // 2. 비즈니스 통계 (목록 필터링, 카운트)

    @Test
    @DisplayName("수신자 삭제 상태 필터링 테스트")
    public void testDeleteMessage() {
        MessageEntity deletedMessage = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("[Test] 메시지 삭제")
                .content("[Test] 삭제된 내용")
                .build();

        deletedMessage.updateReceiverDeleteState(1);
        messageRepository.save(deletedMessage);

        PageRequest pageable = PageRequest.of(0, 10);
        Page<MessageEntity> result = messageRepository
                .findByReceiverAndReceiverDeleteState(receiver, 0, pageable);

        boolean hasDeletedMessage = result.getContent().stream()
                .anyMatch(m -> m.getTitle().equals("[Test] deleted message"));

        assertThat(hasDeletedMessage).isFalse();
        log.info("삭제 필터링 테스트 완료: 목록에 나타나지 않음");
    }

    @Test
    @DisplayName("발신자 삭제 상태 필터링 테스트")
    void testSenderDeleteState() {
        MessageEntity message = MessageEntity.builder()
                .sender(sender).receiver(receiver)
                .title("[TEST]발신자 삭제 테스트")
                .content("[TEST] 내용")
                .build();
        message.updateSenderDeleteState(1);
        messageRepository.save(message);

        Page<MessageEntity> result = messageRepository
                .findBySenderAndSenderDeleteState(sender, 0, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        log.info("발신자 보낸함 필터링 테스트 완료");
    }

    @Test
    @DisplayName("읽지 않은 쪽지 개수 카운트 테스트")
    void testCountUnreadMessages() {
        messageRepository.save(MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("[TEST] 안읽음1")
                .content("[TEST] 내용")
                .build());
        messageRepository.save(MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("[TEST] 안읽음2")
                .content("[TEST] 내용")
                .build());

        MessageEntity readMsg = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("[TEST] 읽음처리")
                .content("[TEST] 내용")
                .build();

        readMsg.markAsRead();
        messageRepository.save(readMsg);

        long unreadCount = messageRepository.countByReceiverAndIsReadAndReceiverDeleteState(receiver, 0, 0);

        assertThat(unreadCount).isEqualTo(2);
        log.info("안 읽은 쪽지 개수 검증 완료: {}개", unreadCount);
    }

    // 3. 데이터 변경 및 삭제 (상태 업데이트, Hard Delete)

    @Test
    @DisplayName("상태 업데이트 테스트 (읽음 처리 시간 검증)")
    void testUpdateMessageStatus() {
        MessageEntity message = messageRepository.save(
                MessageEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .title("[TEST] 읽기 전")
                        .content("[TEST] 내용")
                        .build()
        );

        message.markAsRead();
        messageRepository.saveAndFlush(message);

        MessageEntity updated = messageRepository.findById(message.getId()).get();
        assertThat(updated.getIsRead()).isEqualTo(1);
        assertThat(updated.getReadedAt()).isNotNull();
        log.info("읽음 처리 시간 기록 확인: {}", updated.getReadedAt());
    }

    @Test
    @DisplayName("물리 삭제 테스트 (DB Delete)")
    void testHardDelete() {
        MessageEntity message = messageRepository.save(
                MessageEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .title("[TEST] 완전삭제")
                        .content("[TEST] 내용")
                        .build()
        );

        messageRepository.delete(message);
        messageRepository.flush();

        Optional<MessageEntity> deleted = messageRepository.findById(message.getId());
        assertThat(deleted).isEmpty();
        log.info("DB 물리 삭제 확인 완료");
    }
}