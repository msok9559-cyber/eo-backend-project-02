package com.example.community.service;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MypageService {

    /**
     * 내가 작성한 게시글 목록 조회
     * - 게시판 제목(boardTitle)까지 채워서 반환 (마이페이지 표시용)
     */
    Page<PostDto> getMyPosts(Long userId, Pageable pageable);

    /**
     * 내가 작성한 댓글 목록 조회
     * - commentService 결과를 그대로 반환
     */
    Page<?> getMyComments(Long userId, Pageable pageable);

    List<PostDto> getRecentPosts(Long userId, int limit);

    List<CommentDto> getRecentComments(Long userId, int limit);
}