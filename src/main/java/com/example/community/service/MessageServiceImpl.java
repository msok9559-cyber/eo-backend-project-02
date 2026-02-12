package com.example.community.service;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.MessageRepository;
import com.example.community.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 쪽지 발송
     * 수신자 아이디(username)가 아닌 닉네임(nickname)으로 유저를 조회하여 발송
     */
    @Override
    @Transactional
    public void sendMessage(MessageDto messageDto, String senderUsername) {
        // 1. 발신자(나) 조회
        UserEntity sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("발신자 정보를 찾을 수 없습니다."));

        // 2. 수신자 조회 (닉네임 기준)
        UserEntity receiver = userRepository.findByNickname(messageDto.getReceiverNickname())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 닉네임입니다."));

        // 3. 자기 자신에게 보내기 방지
        if (sender.getUsername().equals(receiver.getUsername())) {
            throw new RuntimeException("자기 자신에게는 쪽지를 보낼 수 없습니다.");
        }

        // 4. 엔티티 빌드 및 저장
        MessageEntity message = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title(messageDto.getTitle())
                .content(messageDto.getContent())
                .build();

        messageRepository.save(message);
    }

    /**
     * 목록 조회
     * type(received/sent/trash/all)에 따른 조건별 최신순 조회
     */
    @Override
    public Page<MessageDto> getMessages(String type, String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if ("received".equals(type)) {
            // 받은 쪽지함: 수신자가 나이고 삭제되지 않은 상태
            return messageRepository.findByReceiverAndReceiverDeleteState(user, 0, pageable)
                    .map(m -> MessageDto.from(m, username));
        } else if ("sent".equals(type)) {
            // 보낸 쪽지함: 발신자가 나이고 삭제되지 않은 상태
            return messageRepository.findBySenderAndSenderDeleteState(user, 0, pageable)
                    .map(m -> MessageDto.from(m, username));
        } else if ("trash".equals(type)) {
            // 휴지통: 수신 혹은 발신 중 하나라도 삭제 상태가 1인 경우
            return messageRepository.findTrashMessages(user, pageable)
                    .map(m -> MessageDto.from(m, username));
        } else {
            // 전체 쪽지함(all): 보낸 쪽지와 받은 쪽지 모두 포함 (deleteState가 0인 것들)
            return messageRepository.findAllMessages(user, pageable)
                    .map(m -> MessageDto.from(m, username));
        }
    }

    /**
     * 쪽지 상세 보기 (수신자일 경우 읽음 처리 포함)
     */
    @Override
    @Transactional
    public Optional<MessageDto> getMessageDetail(Long id, String username) {
        return messageRepository.findById(id).map(message -> {
            // 권한 체크: 발신자나 수신자가 아니면 조회 불가
            if (!message.getSender().getUsername().equals(username) &&
                    !message.getReceiver().getUsername().equals(username)) {
                throw new RuntimeException("조회 권한이 없습니다.");
            }
            // 수신자가 읽는 경우에만 읽음 처리
            if (message.getReceiver().getUsername().equals(username)) {
                message.markAsRead();
            }
            return MessageDto.from(message, username);
        });
    }

    /**
     * 휴지통 이동
     * 발신자/수신자 삭제 상태 업데이트
     */
    @Override
    @Transactional
    public void moveToTrash(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        // 내가 보낸 쪽지 리스트에서 삭제를 눌렀을 때와 받은 리스트에서 눌렀을 때 구분
        if ("sent".equals(userType)) {
            message.updateSenderDeleteState(1);
        } else {
            message.updateReceiverDeleteState(1);
        }
    }

    /**
     * 쪽지 복구 하기
     */
    @Override
    @Transactional
    public void restoreMessage(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        if ("sent".equals(userType)) {
            message.updateSenderDeleteState(0);
        } else {
            message.updateReceiverDeleteState(0);
        }
    }

    /**
     * 쪽지 영구 삭제 (양측 모두 삭제 시 물리 삭제)
     */
    @Override
    @Transactional
    public void permanentDelete(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        if ("sent".equals(userType)) {
            message.updateSenderDeleteState(2);
        } else {
            message.updateReceiverDeleteState(2);
        }

        // 양측 사용자 모두 영구 삭제(state=2)를 요청한 경우 DB에서 실제 데이터 삭제
        if (message.getSenderDeleteState() == 2 && message.getReceiverDeleteState() == 2) {
            messageRepository.delete(message);
        }
    }
}