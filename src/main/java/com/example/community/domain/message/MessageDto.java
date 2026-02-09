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
    @NotBlank(message = "수신자의 이름은 필수 입니다.")
    private String receiverUsername;
    private String receiverNickname;

    @NotBlank(message = "제목을 입력해주세요")
    @Size(min = 1, max = 1000, message = "제목은 1자 이상 1000자 이하로 작성해 주셔야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 1, max = 50, message = "내용은 1자 이상 50자 이하로 작성해 주셔야 합니다.")
    private String content;

    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readedAt;

    /**
     * Entity -> Dto 변환
     */
    public static MessageDto from(@NotNull MessageEntity messageEntity) {
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
                .build();
    }
}
