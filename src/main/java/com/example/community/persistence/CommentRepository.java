package com.example.community.persistence;

import com.example.community.domain.comment.CommentsEntity;
import com.example.community.domain.post.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentsEntity, Long> {
    /**
     * 특정 게시물의 댓글 목록을 조회
     *
     * @param postEntity 확인할 게시물
     * @return 댓글 목록
     */

    List<CommentsEntity> findByPostEntity(PostEntity postEntity);

    /**
     * 특정 게시물의 댓글 목록을 조회
     *
     * @param postId 확인할 게시물의 ID
     * @return 댓글 목록
     */

    List<CommentsEntity> findByPostEntityId(Long postId);
}