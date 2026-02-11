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
     * 발신자 아이디를 로그인 사용자 ID로 저장하며 초기 상태값 설정
     */
    @Override
    @Transactional
    public void sendMessage(MessageDto messageDto, String senderUsername) {
        UserEntity sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("발신자를 찾을 수 없습니다."));
        UserEntity receiver = userRepository.findByUsername(messageDto.getReceiverUsername())
                .orElseThrow(() -> new RuntimeException("수신자를 찾을 수 없습니다."));

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
     * type(수신/발신/휴지통)에 따른 조건별 최신순 조회
     */
    @Override
    public Page<MessageDto> getMessages(String type, String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if ("수신".equals(type)) {
            return messageRepository.findByReceiverAndReceiverDeleteState(user, 0, pageable).map(MessageDto::from);
        } else if ("발신".equals(type)) {
            return messageRepository.findBySenderAndSenderDeleteState(user, 0, pageable).map(MessageDto::from);
        } else if ("휴지통".equals(type)) {
            return messageRepository.findTrashMessages(user, pageable).map(MessageDto::from);
        }
        return Page.empty();
    }

    /**
     * 쪽지 상세 조회
     * 수신자가 조회할 때만 읽음 처리(is_read=1)를 수행함
     */
    @Override
    @Transactional
    public Optional<MessageDto> getMessageDetail(Long id, String username) {
        return messageRepository.findById(id).map(message -> {
            // 권한 체크
            if (!message.getSender().getUsername().equals(username) &&
                    !message.getReceiver().getUsername().equals(username)) {
                throw new RuntimeException("조회 권한이 없습니다.");
            }
            // 수신자가 읽는 경우에만 읽음 처리
            if (message.getReceiver().getUsername().equals(username)) {
                message.markAsRead();
            }
            return MessageDto.from(message);
        });
    }

    /**
     * 휴지통 이동
     * 실제 삭제하지 않고 상태값만 1로 업데이트
     */
    @Override
    @Transactional
    public void moveToTrash(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        if ("발신".equals(userType)) {
            message.updateSenderDeleteState(1);
        } else {
            message.updateReceiverDeleteState(1);
        }
    }

    /**
     * 복구 하기
     * 휴지통 상태(1)를 유지 상태(0)로 변경
     */
    @Override
    @Transactional
    public void restoreMessage(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        if ("발신".equals(userType)) {
            message.updateSenderDeleteState(0);
        } else {
            message.updateReceiverDeleteState(0);
        }
    }

    /**
     * 영구 삭제
     * 상태를 2로 변경하고, 양측 모두 2일 경우에만 물리적 DELETE 수행
     */
    @Override
    @Transactional
    public void permanentDelete(Long id, String username, String userType) {
        MessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        if ("발신".equals(userType)) {
            message.updateSenderDeleteState(2);
        } else {
            message.updateReceiverDeleteState(2);
        }

        // 양측 사용자 모두 영구 삭제를 요청한 경우 DB에서 실제 삭제
        if (message.getSenderDeleteState() == 2 && message.getReceiverDeleteState() == 2) {
            messageRepository.delete(message);
        }
    }
}