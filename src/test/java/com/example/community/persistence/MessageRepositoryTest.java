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

    @Test
    @DisplayName("받은 쪽지함 및 보낸 쪽지함 기본 조회 테스트")
    void testSaveAndFindBasic() {
        MessageEntity message = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("안녕하세요")
                .content("내용입니다")
                .build();
        messageRepository.save(message);

        // 수신자 기준 조회 (deleteState 0: 정상)
        Page<MessageEntity> received = messageRepository
                .findByReceiverAndReceiverDeleteState(receiver, 0, PageRequest.of(0, 10));
        // 발신자 기준 조회 (deleteState 0: 정상)
        Page<MessageEntity> sent = messageRepository
                .findBySenderAndSenderDeleteState(sender, 0, PageRequest.of(0, 10));

        //
        assertThat(received.getContent()).isNotEmpty();
        assertThat(received.getContent().get(0).getTitle()).isEqualTo("안녕하세요");
        assertThat(sent.getContent()).isNotEmpty();
        log.info("기본 송수신 조회 성공");
    }

    @Test
    @DisplayName("휴지통 메시지 통합 조회(@Query) 테스트")
    void testFindTrashMessages() {
        // 내가 보낸 쪽지인데 내가 삭제(휴지통)한 경우
        MessageEntity msg1 = MessageEntity.builder()
                .sender(sender).receiver(receiver).title("발신자삭제").content("내용")
                .build();
        msg1.updateSenderDeleteState(1); // 1: 휴지통
        messageRepository.save(msg1);

        // 내가 받은 쪽지인데 내가 삭제(휴지통)한 경우
        MessageEntity msg2 = MessageEntity.builder()
                .sender(receiver).receiver(sender).title("수신자삭제").content("내용")
                .build();
        msg2.updateReceiverDeleteState(1); // 1: 휴지통
        messageRepository.save(msg2);

        // sender 기준 휴지통 조회 (본인이 발신자이든 수신자이든 상태가 1인 것 모두)
        Page<MessageEntity> trashPage = messageRepository.findTrashMessages(sender, PageRequest.of(0, 10));

        assertThat(trashPage.getTotalElements()).isEqualTo(2);
        log.info("휴지통 통합 조회 성공: {}건", trashPage.getTotalElements());
    }

    @Test
    @DisplayName("전체 쪽지함 통합 조회(@Query) 테스트")
    void testFindAllMessages() {
        // 보낸 쪽지 1개, 받은 쪽지 1개 저장 (삭제 상태 0)
        messageRepository.save(MessageEntity.builder().
                sender(sender)
                .receiver(receiver)
                .title("보낸쪽지")
                .content("내용")
                .build());
        messageRepository.save(MessageEntity.builder()
                .sender(receiver)
                .receiver(sender)
                .title("받은쪽지")
                .content("내용")
                .build());

        // sender 기준 전체 조회 (보낸 것 + 받은 것 중 정상 상태)
        Page<MessageEntity> allPage = messageRepository
                .findAllMessages(sender, PageRequest.of(0, 10));

        assertThat(allPage.getTotalElements()).isEqualTo(2);
        log.info("전체 쪽지함 통합 조회 성공: {}건", allPage.getTotalElements());
    }

    @Test
    @DisplayName("읽지 않은 쪽지 개수 카운트 테스트")
    void testCountUnreadMessages() {
        // 빌더에서 isRead(0)를 호출하지 않음 (엔티티 생성자에서 자동으로 0 세팅)
        messageRepository.save(MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("제목1")
                .content("내용")
                .build());
        messageRepository.save(MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title("제목2")
                .content("내용")
                .build());

        // 읽지 않은(0), 정상상태(0)인 쪽지 카운트
        long unreadCount = messageRepository.countByReceiverAndIsReadAndReceiverDeleteState(receiver, 0, 0);


        assertThat(unreadCount).isEqualTo(2);
        log.info("읽지 않은 쪽지 개수: {}", unreadCount);
    }

    @Test
    @DisplayName("영구 삭제(물리 삭제) 조건 검증 테스트")
    void testPhysicalDeleteCondition() {
        MessageEntity message = messageRepository.save(
                MessageEntity.builder().sender(sender).receiver(receiver).title("영구삭제용").content("내용").build()
        );

        // 양측 모두 영구 삭제 상태(2)로 변경
        message.updateSenderDeleteState(2);
        message.updateReceiverDeleteState(2);
        messageRepository.saveAndFlush(message);

        // 양측 상태가 2일 때만 delete 수행
        if (message.getSenderDeleteState() == 2 && message.getReceiverDeleteState() == 2) {
            messageRepository.delete(message);
        }
        messageRepository.flush();

        Optional<MessageEntity> deleted = messageRepository.findById(message.getId());
        assertThat(deleted).isEmpty();
        log.info("물리 삭제 조건 충족 및 삭제 완료 확인");
    }
}