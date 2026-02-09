package com.example.community.service;

import com.example.community.domain.message.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MessageService {

    /**
     * 쪽지 보내기
     * @param messageDto 보내는 메시지 정보 (수신자 아이디, 제목, 내용 등)
     * @param senderUsername 발신자 아이디 (로그인한 사용자 정보)
     */
    void sendMessage(MessageDto messageDto, String senderUsername);

    /**
     * 받은 쪽지함 조회 (페이징 처리)
     * - 본인이 수신자이며, 삭제하지 않은 쪽지 목록을 반환
     * @param receiverUsername 수신자 아이디
     * @param pageable 페이징 및 정렬 정보
     * @return 검색된 받은 쪽지 목록
     */
    Page<MessageDto> getReceivedMessages(String receiverUsername, Pageable pageable);

    /**
     * 보낸 쪽지함 조회 (페이징 처리)
     * - 본인이 발신자이며, 삭제하지 않은 쪽지 목록을 반환
     * @param senderUsername 발신자 아이디
     * @param pageable 페이징 및 정렬 정보
     * @return 검색된 보낸 쪽지 목록
     */
    Page<MessageDto> getSentMessages(String senderUsername, Pageable pageable);

    /**
     * 쪽지 상세 보기
     * - 해당 쪽지를 읽음 상태로 변경하고 읽은 시간을 기록해야한다.
     * - 조회자가 발신자 혹은 수신자인지 확인하는 보안 필터링이 포함
     * @param messageId 쪽지 번호 (PK)
     * @param username 조회 요청자 아이디 (본인 확인용)
     * @return 조회된 쪽지 상세 정보 (데이터가 없거나 권한이 없으면 Optional.empty 반환)
     */
    Optional<MessageDto> getMessageDetail(Long messageId, String username);

    /**
     * 쪽지 삭제
     * - 실제 DB 데이터를 지우지 않고 발신자/수신자 별로 삭제 상태만 변경
     * @param messageId 쪽지 번호 (PK)
     * @param username 삭제 요청자 아이디
     */
    void deleteMessage(Long messageId, String username);
}