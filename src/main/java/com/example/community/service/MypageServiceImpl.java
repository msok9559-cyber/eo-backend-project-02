package com.example.community.service;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.post.PostDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageServiceImpl implements MypageService {

    private final PostService postService;
    private final CommentService commentService;
    private final BoardService boardService;

    @Override
    public Page<PostDto> getMyPosts(Long userId, Pageable pageable) {
        log.info("마이페이지-내 게시글 조회: userId={}, pageable={}", userId, pageable);

        Page<PostDto> postPage = postService.getMyPosts(userId, pageable);

        // 게시글이 없으면 바로 반환
        if (postPage == null || postPage.getContent().isEmpty()) {
            return postPage;
        }

        // 1) boardId 모으기
        List<Long> boardIds = postPage.getContent().stream()
                .map(PostDto::getBoardId)
                .distinct()
                .toList();

        // 2) boardId -> boardTitle 맵 만들기
        Map<Long, String> boardTitleMap = boardService.getByIds(boardIds).stream()
                .collect(Collectors.toMap(BoardDto::getId, BoardDto::getTitle));

        // 3) postDto에 boardTitle 채우기
        postPage.getContent().forEach(post ->
                post.setBoardTitle(boardTitleMap.getOrDefault(post.getBoardId(), "알 수 없음"))
        );

        return postPage;
    }

    @Override
    public List<PostDto> getRecentPosts(Long userId, int limit) {
        log.info("마이페이지-최근 게시글 조회: userId={}, limit={}", userId, limit);

        List<PostDto> recentPosts = postService.findTop10ByUserId(userId);

        if (recentPosts == null || recentPosts.isEmpty()) {
            return recentPosts;
        }

        List<Long> boardIds = recentPosts.stream()
                .map(PostDto::getBoardId)
                .distinct()
                .toList();

        Map<Long, String> boardTitleMap = boardService.getByIds(boardIds).stream()
                .collect(Collectors.toMap(BoardDto::getId, BoardDto::getTitle));

        recentPosts.forEach(post ->
                post.setBoardTitle(boardTitleMap.getOrDefault(post.getBoardId(), "알 수 없음"))
        );

        return recentPosts;
    }

    @Override
    public List<CommentDto> getRecentComments(Long userId, int limit) {
        log.info("마이페이지-최근 댓글 조회: userId={}, limit={}", userId, limit);

        List<CommentDto> recentComments = commentService.findTop10ByUserId(userId);

        if (recentComments == null || recentComments.isEmpty()) {
            return recentComments;
        }


        return recentComments;
    }

    @Override
    public Page<?> getMyComments(Long userId, Pageable pageable) {
        log.info("마이페이지-내 댓글 조회: userId={}, pageable={}", userId, pageable);
        return commentService.getMyComments(userId, pageable);
    }
}
