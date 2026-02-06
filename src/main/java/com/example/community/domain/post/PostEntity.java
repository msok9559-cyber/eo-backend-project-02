package com.example.community.domain.post;

import com.example.community.domain.comment.CommentEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * posts 테이블과 매핑되는 JPA 엔티티(entity)
 * writer(nickname)은 posts에 저장하지 않고, users 테이블에서 조회해서 DTO를 채운다
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "post_title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // nickname
    // private String writer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount;

    // 게시글 타입 (공지/일반 등, nullable)
    @Column(name = "post_type")
    private Short postType;

    @Column(name = "fixed", nullable = false)
    // 고정글 여부 (일반 - 0, 고정 - 1)
    private Short fixed;

    @ToString.Exclude
    @OneToMany(mappedBy = "postEntity", cascade = CascadeType.REMOVE)
    private List<CommentEntity> commentEntityList = new ArrayList<>();

    @Builder
    public PostEntity(Long userId, Long boardId, String title, String content,
                      Integer viewCount, Integer commentsCount, Integer likesCount, Short postType, Short fixed) {
        this.userId = userId;
        this.boardId = boardId;
        this.title = title;
        this.content = content;

        this.viewCount = (viewCount == null ? 0 : viewCount);
        this.commentsCount = (commentsCount == null ? 0 : commentsCount);
        this.likesCount = (likesCount == null ? 0 : likesCount);

        this.postType = postType;
        this.fixed = (fixed == null ? (short) 0 : fixed);
    }

    // update 부분
    public PostEntity updateTitle(String title) {
        this.title = title;
        return this;
    }

    public PostEntity updateContent(String content) {
        this.content = content;
        return this;
    }

    public PostEntity updatePostType(Short postType) {
        this.postType = postType;
        return this;
    }

    public PostEntity updateFixed(Short fixed) {
        this.fixed = (fixed == null ? (short) 0 : fixed);
        return this;
    }

    public PostEntity increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount + 1);
        return this;
    }
}
