package com.example.community.domain.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    // 쪽지 번호
    private Long id;

    // 표시될 발신자 ID
    private String senderUsername;
    private String senderNickname;

    // 입력 받은 수신자 ID
    @NotBlank(message = "수신자의 닉네임은 필수 입니다.")
    private String receiverNickname;
    private String receiverUsername;

    @NotBlank(message = "제목을 입력해주세요")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하로 작성해 주셔야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 1, max = 1000, message = "내용은 1자 이상 1000자 이하로 작성해 주셔야 합니다.")
    private String content;

    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readedAt;

    // 타임리프(message.html)에서 사용하는 가상 필드
    private String type;            // RECEIVED(수신) 또는 SENT(발신) 상태 저장
    private String displayNickname;  // 표시할 상대방의 닉네임

    /**
     * Entity -> Dto 변환
     * 현재 로그인 유저 정보를 받아 type과 displayNickname을 계산하여 생성
     */
    public static MessageDto from(@NotNull MessageEntity messageEntity, String currentUsername) {
        // 현재 사용자가 수신자인지 발신자인지 판별
        String mType = messageEntity.getReceiver().getUsername().equals(currentUsername) ? "RECEIVED" : "SENT";

        // 타입에 따라 화면에 노출할 상대방 닉네임 설정
        String dNick = mType.equals("RECEIVED")
                ? messageEntity.getSender().getNickname()
                : messageEntity.getReceiver().getNickname();

        return MessageDto.builder()
                .id(messageEntity.getId())
                .senderUsername(messageEntity.getSender().getUsername())
                .senderNickname(messageEntity.getSender().getNickname())
                .receiverUsername(messageEntity.getReceiver().getUsername())
                .receiverNickname(messageEntity.getReceiver().getNickname())
                .title(messageEntity.getTitle())
                .content(messageEntity.getContent())
                .isRead(messageEntity.getIsRead())
                .createdAt(messageEntity.getCreatedAt())
                .readedAt(messageEntity.getReadedAt())
                .type(mType)
                .displayNickname(dNick)
                .build();
    }
}