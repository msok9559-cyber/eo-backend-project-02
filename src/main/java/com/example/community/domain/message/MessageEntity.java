package com.example.community.domain.message;

import com.example.community.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@ToString(exclude = {"sender", "receiver"})
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class MessageEntity {

    /**
     * 쪽지의 고유 식별 번호 Primary Key
     * IDENTITY = AUTO-INCREMENT
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 발신자 정보 users 테이블의 ID를 참조
     * fetch 타입 EAGER : 바로 조회기능
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    /**
     * 수신자 정보 users 테이블의 ID를 참조
     * fetch 타입 EAGER : 바로 조회기능
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // 쪽지 제목 조건 : 최대 50자
    @Column(name ="m_title", nullable = false, length = 50)
    private String title;

    // 쪽지 내용 조건 : 최대 1000자
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // 읽음 여부 : 0 = 읽지않음 1 = 읽음
    @Column(name = "is_read", nullable = false)
    private Integer isRead = 0;

    /**
     * CreationTimestamp 자동으로 생성된 데이터의 시간을 기록
     * 보낸 시간을 저장
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 읽은 시간을 저장
     */
    @Column(name = "readed_at")
    private LocalDateTime readedAt;

    /**
     * 보낸 메시지를 사용자에게만 안보이게 처리
     * 발신자 삭제 여부 처리 0 = 유지, 1 = 삭제
     */
    @Column(name = "sender_delete_state", nullable = false)
    private Integer senderDeleteState = 0;

    /**
     * 받은 메시지를 사용자에게만 안보이게 처리
     * 수신자 삭제 여부 처리 0 = 유지, 1 = 삭제
     */
    @Column(name = "receiver_delete_state", nullable = false)
    private Integer receiverDeleteState = 0;

    @Builder
    public MessageEntity(UserEntity sender, UserEntity receiver, String title, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.title = title;
        this.content = content;
        this.isRead = 0;
        this.senderDeleteState = 0;
        this.receiverDeleteState = 0;
    }

    /**
     * 쪽지 읽음 처리
     * 상태를 1로 바꾸고 현재 시간을 저장
     */
    public void markAsRead() {
        if (this.isRead == 0) {
            this.isRead = 1;
            this.readedAt = LocalDateTime.now();
        }
    }

    /**
     * 발신자 쪽 삭제 처리
     * @param senderDeleteState 발신자 상태값
     */
    public void updateSenderDeleteState(Integer senderDeleteState) { this.senderDeleteState = senderDeleteState; }

    /**
     * 수신자 쪽 삭제 처리
     * @param receiverDeleteState 수신자 상태 값
     */
    public void updateReceiverDeleteState(Integer receiverDeleteState) { this.receiverDeleteState = receiverDeleteState; }
}