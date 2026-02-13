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

    // 댓글 고유 식별 번호 pk
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 고유 번호
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 게시물 번호
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    // 댓글 내용
    @Column(name = "r_content", nullable = false, length = 200)
    private String content;

    // 생성 일자
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일자
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

//    @Column(name = "comments_count", nullable = false)
//    private Integer commentsCount;
//
     // 빌더 패턴
    @Builder
    public CommentEntity(Long userId, PostEntity postEntity, String content) {
    this.userId = userId;
    this.postEntity = postEntity;
    this.content = content;
//
//        this.commentsCount = (commentsCount == null ? 0 : commentsCount);
    }

    // 수정
    public CommentEntity updateContent(String content) {
        this.content = content;
        return this;
    }

//    public CommentEntity increaseCommentsCount() {
//        this.commentsCount = (this.commentsCount == null ? 0 : this.commentsCount + 1);
//        return this;
//    }


}
