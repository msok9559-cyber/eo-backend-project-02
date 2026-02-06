package com.example.community.service;

import com.example.community.domain.board.BoardEntity;
import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
//    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole())
                .map(role -> "ROLE_ADMIN".equals(role.toString()))
                .orElse(false);
    }

    @Override
    public Long create(Long boardId, PostDto postDto, Long userId) {
        log.info("CREATE: boardId={}, postDto={}, userId={}", boardId, postDto, userId);

        // 게시판 존재 확인
//        boardRepository.findById(boardId)
//                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + boardId));

        PostEntity postEntity = PostEntity.builder()
                .boardId(boardId)
                .userId(userId)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .postType(postDto.getPostType())
                .fixed(postDto.getFixed())
                .viewCount(0)
                .commentsCount(0)
                .likesCount(0)
                .build();

        PostEntity savedEntity = postRepository.save(postEntity);
        log.info("CREATE: saved={}", savedEntity);

        postDto.setId(savedEntity.getId());
        return savedEntity.getId();
    }

    @Override
    @Transactional
    public PostDto read(Long id) {
        log.info("READ: id = {}", id);

        // 게시글 조회
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow( () -> new EntityNotFoundException("Post not found: " + id));

        // 조회수 증가
        postEntity.increaseViewCount();

        String nickname = "unknown";

        try {
            // 작성자 닉네임 조회
            nickname = userRepository.findById(postEntity.getUserId())
                    .map(user -> user.getNickname())
                    .orElse("unknown");
        } catch (Exception e) {
            log.warn("READ: failed to load writer nickname. userId={}, reason={}",
                    postEntity.getUserId(), e.getMessage());
        }


        return PostDto.from(postEntity, nickname);

    }

    @Override
    @Transactional
    public boolean update(PostDto postDto, Long userId) {
        log.info("UPDATE: postId={}, userId={}, title={}", postDto.getId(), userId, postDto.getTitle());

        return postRepository.findById(postDto.getId()).map(postEntity -> {
                    //작성자 검증
                    if (!postEntity.getUserId().equals(userId)) {
                        log.info("UPDATE DENIED: postId={}, requestUserId={}, ownerUserId={}",
                                postDto.getId(), userId, postEntity.getUserId());
                        return false;
                    }

                    if (postDto.getTitle() != null) postEntity.updateTitle(postDto.getTitle());
                    if (postDto.getContent() != null) postEntity.updateContent(postDto.getContent());
                    postEntity.updatePostType(postDto.getPostType());
                    postEntity.updateFixed(postDto.getFixed());

                    postRepository.save(postEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long userId) {
        log.info("DELETE: id = {}, userId={}", id, userId);

        return postRepository.findById(id).map(postEntity -> {
            boolean isOwner = postEntity.getUserId().equals(userId);
            boolean admin = isAdmin(userId);

            if (!isOwner && !admin) {
                log.info("DELETE DENIED: postId={}, requestUserId={}, ownerUserId={}, isAdmin={}",
                        id, userId, postEntity.getUserId(), admin);
                return false;
            }

            postRepository.delete(postEntity);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public Page<PostDto> getList(Long boardId, Pageable pageable) {
        log.info("GET LIST: boardId={}, pageable={}", boardId, pageable);

        return postRepository.findByBoardId(boardId, pageable)
                .map(postEntity -> {
                    String nickname = userRepository.findById(postEntity.getUserId())
                            .map(user -> user.getNickname())
                            .orElse("unknown");

                    return PostDto.from(postEntity, nickname);
                });

    }
}
