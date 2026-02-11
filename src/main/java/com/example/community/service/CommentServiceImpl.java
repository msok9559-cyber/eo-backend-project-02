package com.example.community.service;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.comment.CommentEntity;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.CommentRepository;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
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
    private final UserRepository userRepository;

    private boolean isOwner(CommentEntity commentEntity, Long userId) {
        return commentEntity.getUserId().equals(userId);
    }

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole().name())   // 예: ADMIN / USER (네 UserRole 기준)
                .map(r -> r.equals("ADMIN"))     // CustomUserDetails에서 ROLE_ 붙여 쓰고 있으니 여기선 name만 비교
                .orElse(false);
    }

    private boolean isUser(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole().name())
                .map(r -> r.equals("USER"))
                .orElse(false);
    }

    @Override
    public Optional<CommentDto> create(CommentDto commentDto, Long userId) {
        if (userId == null) return Optional.empty();

        if (isAdmin(userId)) {
            log.info("CREATE DENIED: admin cannot create comment. userId={}", userId);
            return Optional.empty();
        }

        if (!isUser(userId)) {
            log.info("CREATE DENIED: not a USER. userId={}", userId);
            return Optional.empty();
        }

        return postRepository.findById(commentDto.getPostId())
                .map(postEntity -> {
                    CommentEntity saved = commentRepository.save(
                            CommentEntity.builder()
                                    .userId(userId)
                                    .postEntity(postEntity)
                                    .content(commentDto.getContent())
                                    .build()
                    );
                    return CommentDto.from(saved);
                });
    }

    @Override
    public Optional<CommentDto> update(CommentDto commentDto, Long userId) {
        if (userId == null) return Optional.empty();

        if (isAdmin(userId)) {
            log.info("UPDATE DENIED: admin cannot update comment. userId={}", userId);
            return Optional.empty();
        }

        return commentRepository.findById(commentDto.getId())
                .filter(comment -> isOwner(comment, userId))
                .map(comment -> {
                    comment.updateContent(commentDto.getContent());
                    return CommentDto.from(commentRepository.save(comment));
                });
    }

    @Override
    public boolean delete(Long id, Long userId) {
        if (userId == null) return false;

        return commentRepository.findById(id)
                .filter(comment -> isOwner(comment, userId) || isAdmin(userId))
                .map(comment -> {
                    commentRepository.delete(comment);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Optional<CommentDto> read(Long id) {
        return commentRepository.findById(id)
                .map(CommentDto::from);
    }

    @Override
    public List<CommentDto> getList(Long postId) {
        return commentRepository.findByPostEntityId(postId).stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentDto> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(CommentDto::from);
    }


}
