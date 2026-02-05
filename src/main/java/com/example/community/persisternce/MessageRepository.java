package com.example.community.persisternce;

import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 받은 쪽지함 조회: 수신자가 '나'이고, 수신자 삭제 상태가 0인 것들을 최신순으로
    List<MessageEntity> findByReceiverAndReceiverDeleteStateOrderByCreatedAtDesc(UserEntity receiver, int deleteState);

    // 보낸 쪽지함 조회: 발신자가 '나'이고, 발신자 삭제 상태가 0인 것들을 최신순으로
    List<MessageEntity> findBySenderAndSenderDeleteStateOrderByCreatedAtDesc(UserEntity sender, int deleteState);

    // 읽지 않은 쪽지 개수 카운트 (알림용)
    long countByReceiverAndIsReadAndReceiverDeleteState(UserEntity receiver, int isRead, int deleteState);
}
