package com.example.community.service;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.comment.CommentEntity;
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
                .map(u -> u.getRole().name())   // ADMIN / USER
                .map(r -> r.equals("ADMIN"))
                .orElse(false);
    }

    private boolean isUser(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole().name())
                .map(r -> r.equals("USER"))
                .orElse(false);
    }


    private String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getNickname())
                .orElse("unknown");
    }

    private CommentDto convertToDto(CommentEntity comment) {
        String nickname = getNickname(comment.getUserId());
        return CommentDto.from(comment, nickname);
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
                    return convertToDto(saved);
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
                    CommentEntity saved = commentRepository.save(comment);
                    return convertToDto(saved);
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
                .map(this::convertToDto);
    }

    @Override
    public List<CommentDto> getList(Long postId) {
        // 정렬 메서드 추가했으면 그걸로 바꿔도 됨
        return commentRepository.findByPostEntityId(postId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentDto> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(this::convertToDto);
    }
}
