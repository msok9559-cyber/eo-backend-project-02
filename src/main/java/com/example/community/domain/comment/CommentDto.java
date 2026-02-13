package com.example.community.domain.comment;

import com.example.community.domain.post.PostEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long userId;
    private Long postId;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 1, max = 200, message = "내용은 1자 이상 200자 이하로 작성해 주셔야 합니다.")
    private String content;


    private String writer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private Integer commentsCount;

    // 기존 from (writer는 비어있음)
    public static CommentDto from(@NotNull CommentEntity commentEntity) {
        return CommentDto.builder()
                .id(commentEntity.getId())
                .userId(commentEntity.getUserId())
                .postId(commentEntity.getPostEntity().getId())
                .content(commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .updatedAt(commentEntity.getUpdatedAt())
                .build();
    }

    public static CommentDto from(@NotNull CommentEntity commentEntity, String writer) {
        CommentDto dto = from(commentEntity);
        dto.setWriter(writer);
        return dto;
    }

    /**
     * CommentDto -> CommentEntity
     * userId는 Service에서 직접 세팅할거라 여기서는 안 넣어도 됨
     */
    public CommentEntity toEntity(PostEntity postsEntity) {
        return CommentEntity.builder()
                .postEntity(postsEntity)
                .content(content)
                .build();
    }
}
