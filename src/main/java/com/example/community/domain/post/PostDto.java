package com.example.community.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 게시글 정보를 계층 간에 전달하기 위한 DTO (Data Transfer Object)
 * ERD의 posts 테이블 중심
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private Long userId;
    private Long boardId;
    private String title;
    private String content;
    // nickname
    private String writer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer viewCount;
    private Integer commentsCount;
    private Integer likesCount;

    // 게시글 타입 (공지/일반 등, nullable)
    private Short postType;
    // 고정글 여부 (일반 - 0, 고정 - 1)
    private Short fixed;

    public static PostDto from(PostEntity postEntity, String writerNickname) {
        if (postEntity == null) {
            throw new IllegalArgumentException("postEntity cannot be null");
        }

        return PostDto.builder()
                .id(postEntity.getId())
                .userId(postEntity.getUserId())
                .boardId(postEntity.getBoardId())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                // 닉네임이 나오게
                .writer(writerNickname)
                .createdAt(postEntity.getCreatedAt())
                .updatedAt(postEntity.getUpdatedAt())
                .viewCount(postEntity.getViewCount())
                .commentsCount(postEntity.getCommentsCount())
                .likesCount(postEntity.getLikesCount())
                .postType(postEntity.getPostType())
                .fixed(postEntity.getFixed())
                .build();
    }
}
