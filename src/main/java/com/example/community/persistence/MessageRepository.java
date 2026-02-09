package com.example.community.persistence;

import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 1. 받은 쪽지함 조회
    Page<MessageEntity> findByReceiverAndReceiverDeleteState(UserEntity receiver, Integer deleteState, Pageable pageable);

    // 2. 보낸 쪽지함 조회
    Page<MessageEntity> findBySenderAndSenderDeleteState(UserEntity sender, Integer deleteState, Pageable pageable);

    // 3. 읽지 않은 쪽지 개수
    long countByReceiverAndIsReadAndReceiverDeleteState(UserEntity receiver, Integer isRead, Integer receiverDeleteState);
}
