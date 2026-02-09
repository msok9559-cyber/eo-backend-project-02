package com.example.community.domain.comment;
import com.example.community.domain.post.PostEntity;
import com.example.community.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "postEntity")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    @Column(name = "r_content", nullable = false, length = 200)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount;

    @Builder
    public CommentEntity(Long userId, PostEntity postEntity, String content) {
        this.userId = userId;
        this.postEntity = postEntity;
        this.content = content;

        this.commentsCount = (commentsCount == null ? 0 : commentsCount);
    }

    public CommentEntity updateContent(String content) {
        this.content = content;
        return this;
    }

    public CommentEntity increaseCommentsCount() {
        this.commentsCount = (this.commentsCount == null ? 0 : this.commentsCount + 1);
        return this;
    }
}
