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
     * 발신자와 수신자를 확인하고 새로운 쪽지 엔티티를 생성하여 저장함
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
     * 받은 쪽지함 조회
     * 수신자 본인의 아이디와 삭제되지 않은 상태(0)인 쪽지들을 페이징하여 가져옴
     */
    @Override
    public Page<MessageDto> getReceivedMessages(String receiverUsername, Pageable pageable) {
        return userRepository.findByUsername(receiverUsername)
                .map(user -> messageRepository.findByReceiverAndReceiverDeleteState(user, 0, pageable)
                        .map(MessageDto::from))
                .orElse(Page.empty());
    }

    /**
     * 보낸 쪽지함 조회
     * 발신자 본인이 보낸 쪽지 중 본인이 삭제하지 않은 상태(0)인 목록을 조회
     */
    @Override
    public Page<MessageDto> getSentMessages(String senderUsername, Pageable pageable) {
        return userRepository.findByUsername(senderUsername)
                .map(user -> messageRepository.findBySenderAndSenderDeleteState(user, 0, pageable)
                        .map(MessageDto::from))
                .orElse(Page.empty());
    }

    /**
     * 쪽지 상세 보기
     * 쪽지 내용을 조회하며, 조회자가 수신자일 경우 '읽음' 상태로 변경함
     */
    @Override
    @Transactional
    public Optional<MessageDto> getMessageDetail(Long messageId, String username) {
        return messageRepository.findById(messageId)
                .filter(m -> m.getSender().getUsername().equals(username) ||
                        m.getReceiver().getUsername().equals(username))
                .map(m -> {
                    if (m.getReceiver().getUsername().equals(username)) {
                        m.markAsRead();
                    }
                    return MessageDto.from(m);
                });
    }

    /**
     * 쪽지 삭제
     * 실제 데이터를 지우지 않고, 요청자가 발신자인지 수신자인지에 따라 각각의 삭제 상태값만 1로 바꿈
     */
    @Override
    @Transactional
    public void deleteMessage(Long messageId, String username) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getSender().getUsername().equals(username)) {
                message.updateSenderDeleteState(1);
            }
            else if (message.getReceiver().getUsername().equals(username)) {
                message.updateReceiverDeleteState(1);
            }
        });
    }
}