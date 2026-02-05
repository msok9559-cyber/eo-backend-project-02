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

    // 입력 받은 수신자 ID
    @NotBlank(message = "수신자의 이름은 필수 입니다.")
    private String receiverUsername;

    @NotNull(message = "제목을 입력해주세요.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하로 작성해 주셔야 합니다..")
    private String title;

    @NotNull(message = "내용을 입력해주세요")
    @Size(min = 1, max = 1000, message = "내용은 1자 이상 1000자 이하로 작성해 주셔야 합니다.")
    private String content;

    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readedAt;

    public static MessageDto from(@NotNull MessageEntity messagesEntity) {
        return MessageDto.builder()
                .id(messagesEntity.getId())
                .senderUsername(messagesEntity.getSender().getUsername())
                .receiverUsername(messagesEntity.getReceiver().getUsername())
                .title(messagesEntity.getTitle())
                .isRead(messagesEntity.getIsRead())
                .createdAt(messagesEntity.getCreatedAt())
                .readedAt(messagesEntity.getReadedAt())
                .build();
    }
}
