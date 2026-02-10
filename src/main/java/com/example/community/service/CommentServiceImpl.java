package com.example.community.service;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.comment.CommentEntity;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.CommentRepository;
import com.example.community.persistence.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /* =====================
       권한 판단 메서드
     ===================== */

    private boolean isOwner(CommentEntity commentEntity, Long userId) {
        return commentEntity.getUserId().equals(userId);
    }

    private boolean isAdmin(Long userId) {
        // 지금은 관리자 판단 로직이 없으므로 false
        // 나중에 UserRepository 연결하면 여기만 수정하면 됨
        return false;
    }

    @Override
    public Page<CommentDto> getAllComments(Pageable pageable) {
        log.info("Get all comments - pageable: {}", pageable);

        return commentRepository.findAll(pageable)
                .map(CommentDto::from);
    }

    /* =====================
       댓글 생성
     ===================== */

    @Override
    public Optional<CommentDto> create(CommentDto commentDto, Long userId) {
        log.info("create = {}, userId={}", commentDto, userId);

        if (userId == null) {
            log.info("CREATE DENIED: not logged in");
            return Optional.empty();
        }

        return postRepository.findById(commentDto.getPostId())
                .map(postEntity -> {
                    CommentEntity commentEntity = CommentEntity.builder()
                            .userId(userId)
                            .postEntity(postEntity)
                            .content(commentDto.getContent())
                            .build();

                    CommentEntity savedEntity = commentRepository.save(commentEntity);

                    return CommentDto.from(savedEntity);
                });
    }

    /* =====================
       댓글 조회 (누구나 가능)
     ===================== */

    @Override
    public Optional<CommentDto> read(Long id) {
        log.info("read = {}", id);

        return commentRepository.findById(id)
                .map(CommentDto::from);
    }

    /* =====================
       댓글 수정 (작성자만)
     ===================== */

    @Override
    public Optional<CommentDto> update(CommentDto commentDto, Long userId) {
        log.info("update = {}, userId={}", commentDto, userId);

        return commentRepository.findById(commentDto.getId())
                .filter(comment -> isOwner(comment, userId))
                .map(comment -> {
                    comment.updateContent(commentDto.getContent());
                    CommentEntity savedEntity = commentRepository.save(comment);
                    return CommentDto.from(savedEntity);
                });
    }

    /* =====================
       댓글 삭제 (작성자 OR 관리자)
     ===================== */

    @Override
    public boolean delete(Long id, Long userId) {
        log.info("delete = {}, userId={}", id, userId);

        return commentRepository.findById(id)
                .filter(comment ->
                        isOwner(comment, userId) || isAdmin(userId)
                )
                .map(comment -> {
                    commentRepository.delete(comment);
                    return true;
                })
                .orElse(false);
    }

    /* =====================
       댓글 목록 조회
     ===================== */

    @Override
    public List<CommentDto> getList(Long postId) {
        log.info("getList = {}", postId);

        return commentRepository.findByPostEntityId(postId)
                .stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
    }
}
