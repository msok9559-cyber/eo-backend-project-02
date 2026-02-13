package com.example.community.service;


import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    // 댓글 등록
    Optional<CommentDto> create(CommentDto commentDto, Long userId);
    // 댓글 조회
    Optional<CommentDto> read(Long id);
    // 댓글 수정
    Optional<CommentDto> update(CommentDto commentDto, Long userId);
    // 댓글 삭제
    boolean delete(Long id, Long userId);
    // 게시물 별 댓글 목록
    List<CommentDto> getList(Long postId);

    // 전체 댓글 조회용
    Page<CommentDto> getAllComments(Pageable pageable);

    /**
     * 내가 작성한 댓글 목록 조회
     * @param userId 작성자(로그인 사용자) ID
     * @param pageable 페이징 정보
     * @return 댓글 달린 게시물
     */
    Page<CommentDto> getMyComments(Long userId, Pageable pageable);
}

