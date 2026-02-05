package com.example.community.domain.comment;

import com.example.community.domain.post.PostEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long postId;

    @NotBlank(message = "Content is required.")
    private String content;

    @NotBlank(message = "Writer is required.")
    private String writer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드(static factory method)
    public static CommentDto from(CommentsEntity commentEntity) {
        return CommentDto.builder()
                .id(commentEntity.getId())
                .postId(commentEntity.getPostEntity().getId())
                .content(commentEntity.getContent())
                .writer(commentEntity.getWriter())
                .createdAt(commentEntity.getCreatedAt())
                .updatedAt(commentEntity.getUpdatedAt())
                .build();
    }

    /**
     * CommentDto 인스턴스를 CommentEntity 인스턴스로 변환
     *
     * @param postEntity CommentEntity 인스턴스가 참조하는 PostEntity 인스턴스
     * @return CommentEntity 인스턴스
     */
    public CommentsEntity toEntity(PostEntity postEntity) {
        return CommentsEntity.builder()
                .postEntity(postEntity)
                .content(content)
                .writer(writer)
                .build();
    }
}