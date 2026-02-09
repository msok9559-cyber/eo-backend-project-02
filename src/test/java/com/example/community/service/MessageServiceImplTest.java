package com.example.community.service;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    private UserEntity sender;
    private UserEntity receiver;

    /**
     * 테스트용 생성
     * 각 테스트 실행 전, 발신자와 수신자 유저를 DB에 미리 생성해둡니다.
     */
    @BeforeEach
    void setUp() {
        sender = UserEntity.builder()
                .username("sender1")
                .password("1234")
                .nickname("보낸사람")
                .name("발신자이름")
                .email("sender@test.com")
                .build();

        userRepository.save(sender);

        receiver = UserEntity.builder()
                .username("receiver1")
                .password("1234")
                .nickname("받는사람")
                .name("수신자이름")
                .email("receiver@test.com")
                .build();

        userRepository.save(receiver);
    }

    /**
     * 쪽지 발송 성공
     * 서비스의 sendMessage 호출 시 데이터가 정확히 저장되고 수신자 목록에 나타나는지 확인
     */
    @Test
    @DisplayName("쪽지 발송 서비스 테스트: 발신과 수신함 확인")
    void sendMessageSuccess() {
        // 1. 전송할 데이터 DTO 준비
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername(receiver.getUsername())
                .title("[TEST] 안녕하세요")
                .content("[TEST] 반갑습니다.")
                .build();

        // 2. 메시지 전송 로직 수행
        messageService.sendMessage(requestDto, sender.getUsername());

        // 3. 수신자 아이디로 받은 쪽지함을 조회하여 전송 결과 검증
        Page<MessageDto> result = messageService.getReceivedMessages(receiver.getUsername(), PageRequest.of(0, 10));
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("안녕하세요");
    }

    /**
     * 상세 조회 및 부가 효과(읽음 처리) 확인
     * 수신자가 쪽지를 읽었을 때, 상태값(isRead)과 읽은 시간(readedAt)이 갱신되는지 확인합니다.
     */
    @Test
    @DisplayName("쪽지 상세 조회: 읽음 처리 및 권한 검증")
    void getMessageDetailTest() {
        // 1. 테스트용 쪽지 발송
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername(receiver.getUsername())
                .title("[TEST] 비밀 쪽지")
                .content("[TEST] 내용")
                .build();
        messageService.sendMessage(requestDto, sender.getUsername());

        // 2. 저장된 쪽지 ID를 조회하여 가져오기
        Long msgId = messageService.getReceivedMessages(receiver.getUsername(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        // 3. 수신자가 본인의 아이디로 쪽지 상세 보기 호출
        Optional<MessageDto> detail = messageService.getMessageDetail(msgId, receiver.getUsername());

        //  Optional 데이터 존재 여부와 읽음 처리 자동 반영(Dirty Checking) 확인
        assertThat(detail).isPresent();
        assertThat(detail.get().getIsRead()).isEqualTo(1);
        assertThat(detail.get().getReadedAt()).isNotNull();
    }

    /**
     * 보안 및 권한 필터링 검증
     * 관계없는 제3자가 쪽지 ID를 알고 있더라도 조회가 불가능(Empty 반환)
     */
    @Test
    @DisplayName("권한 없는 유저가 상세 조회 시 Optional.empty를 반환해야 한다")
    void getMessageDetailInvalidUser() {
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername(receiver.getUsername()).title("T").content("C").build();
        messageService.sendMessage(requestDto, sender.getUsername());

        Long msgId = messageService.getReceivedMessages(receiver.getUsername(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        Optional<MessageDto> detail = messageService.getMessageDetail(msgId, "anonymousUser");


        assertThat(detail).isEmpty();
    }

    /**
     * 논리 삭제 처리 검증
     * 수신자가 쪽지를 삭제했을 때, DB에서 데이터가 바로 지워지는 것이 아니라 '받은 쪽지함' 목록에서만 사라져야 한다.
     */
    @Test
    @DisplayName("쪽지 삭제 테스트: 삭제 후 목록에 나타나지 않아야 함")
    void deleteMessageTest() {
        // 1. 테스트용 쪽지 발송
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername(receiver.getUsername())
                .title("[TEST] 삭제용")
                .content("[TEST] 내용")
                .build();
        messageService.sendMessage(requestDto, sender.getUsername());

        Long msgId = messageService.getReceivedMessages(receiver.getUsername(), PageRequest.of(0, 10))
                .getContent().get(0).getId();

        // 2. 수신자가 본인의 이름으로 해당 쪽지 삭제 호출
        messageService.deleteMessage(msgId, receiver.getUsername());

        // 3. 다시 받은 쪽지함을 조회했을 때 해당 쪽지가 검색되지 않아야 함
        Page<MessageDto> result = messageService.getReceivedMessages(receiver.getUsername(), PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }

    /**
     * 예외 상황 처리 검증 (에러 핸들링)
     * 수신자 아이디가 DB에 없는 가짜 아이디일 경우 시스템이 예외를 적절히 던지는지 확인합니다.
     */
    @Test
    @DisplayName("존재하지 않는 수신자에게 발송 시 예외 발생")
    void sendToNonExistUser() {
        // 1. 존재하지 않는 아이디("no_user")를 수신자로 설정
        MessageDto requestDto = MessageDto.builder()
                .receiverUsername("no_user")
                .title("[TEST] title")
                .content("[TEST] content")
                .build();

        // 2. 발송 시 RuntimeException이 생기는지 검증

        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(requestDto, sender.getUsername());
        });
    }
}