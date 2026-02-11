package com.example.community.service;

import com.example.community.domain.message.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MessageService {

    /**
     * 쪽지 보내기
     */
    void sendMessage(MessageDto messageDto, String senderUsername);

    /**
     * 쪽지 목록 조회 (타입별: 수신/발신/휴지통)
     */
    Page<MessageDto> getMessages(String type, String username, Pageable pageable);

    /**
     * 쪽지 상세 보기 (수신자일 경우 읽음 처리 포함)
     */
    Optional<MessageDto> getMessageDetail(Long id, String username);

    /**
     * 쪽지 휴지통으로 이동 (Soft Delete)
     */
    void moveToTrash(Long id, String username, String userType);

    /**
     * 쪽지 복구 하기
     */
    void restoreMessage(Long id, String username, String userType);

    /**
     * 쪽지 영구 삭제 (양측 모두 삭제 시 물리 삭제)
     */
    void permanentDelete(Long id, String username, String userType);
}