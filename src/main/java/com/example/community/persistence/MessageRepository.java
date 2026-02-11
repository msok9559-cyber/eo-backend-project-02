package com.example.community.persistence;

import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 1. 받은 쪽지함 조회 (기존 유지)
    Page<MessageEntity> findByReceiverAndReceiverDeleteState(UserEntity receiver, Integer deleteState, Pageable pageable);

    // 2. 보낸 쪽지함 조회 (기존 유지)
    Page<MessageEntity> findBySenderAndSenderDeleteState(UserEntity sender, Integer deleteState, Pageable pageable);

    /**
     * 3. 휴지통 조회
     * 수신자이면서 수신자삭제상태가 1이거나, 발신자이면서 발신자삭제상태가 1인 경우 조회
     */
    @Query("SELECT m FROM MessageEntity m WHERE " +
            "(m.receiver = :user AND m.receiverDeleteState = 1) OR " +
            "(m.sender = :user AND m.senderDeleteState = 1)")
    Page<MessageEntity> findTrashMessages(@Param("user") UserEntity user, Pageable pageable);

    // 4. 읽지 않은 쪽지 개수 (기존 유지)
    long countByReceiverAndIsReadAndReceiverDeleteState(UserEntity receiver, Integer isRead, Integer receiverDeleteState);
}