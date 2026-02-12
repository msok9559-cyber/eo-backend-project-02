package com.example.community.persistence;

import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 1. 받은 쪽지함 조회
    Page<MessageEntity> findByReceiverAndReceiverDeleteState(UserEntity receiver, Integer deleteState, Pageable pageable);

    // 2. 보낸 쪽지함 조회
    Page<MessageEntity> findBySenderAndSenderDeleteState(UserEntity sender, Integer deleteState, Pageable pageable);

    /**
     * 3. 휴지통 조회
     */
    @Query("SELECT m FROM MessageEntity m WHERE " +
            "(m.receiver = :user AND m.receiverDeleteState = 1) OR " +
            "(m.sender = :user AND m.senderDeleteState = 1)")
    Page<MessageEntity> findTrashMessages(@Param("user") UserEntity user, Pageable pageable);

    /**
     * 4. 전체 쪽지함 조회 (보낸 것 + 받은 것 통합)
     */
    @Query("SELECT m FROM MessageEntity m WHERE " +
            "(m.receiver = :user AND m.receiverDeleteState = 0) OR " +
            "(m.sender = :user AND m.senderDeleteState = 0)")
    Page<MessageEntity> findAllMessages(@Param("user") UserEntity user, Pageable pageable);

    // 5. 읽지 않은 쪽지 개수
    long countByReceiverAndIsReadAndReceiverDeleteState(UserEntity receiver, Integer isRead, Integer receiverDeleteState);
}