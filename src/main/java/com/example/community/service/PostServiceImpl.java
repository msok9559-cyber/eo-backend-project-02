package com.example.community.service;

import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
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
    private final UserRepository userRepository;

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    @Override
    public Long create(Long boardId, PostDto postDto, Long userId) {
        log.info("CREATE: boardId={}, postDto={}, userId={}", boardId, postDto, userId);

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
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

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
                    // 작성자 검증
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
            postRepository.flush();

            return true;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getList(Long boardId, Pageable pageable) {
        log.info("GET LIST: boardId={}, pageable={}", boardId, pageable);

        return postRepository.findByBoardId(boardId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        log.info("GET ALL POSTS: pageable={}", pageable);

        return postRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String searchType, String keyword, Pageable pageable) {
        log.info("SEARCH POSTS: searchType={}, keyword={}, pageable={}", searchType, keyword, pageable);

        // 키워드가 비어있으면 전체 목록 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts(pageable);
        }

        Page<PostEntity> resultPage;

        switch (searchType) {
            case "title":
                resultPage = postRepository.searchByTitle(keyword, pageable);
                break;
            case "content":
                resultPage = postRepository.searchByContent(keyword, pageable);
                break;
            case "writer":
                resultPage = postRepository.searchByWriter(keyword, pageable);
                break;
            case "titleContent":
                resultPage = postRepository.searchByTitleOrContent(keyword, pageable);
                break;
            default:
                log.warn("SEARCH: invalid searchType={}, using titleContent", searchType);
                resultPage = postRepository.searchByTitleOrContent(keyword, pageable);
        }

        return resultPage.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPosts(Pageable pageable) {
        log.info("GET POPULAR POSTS: pageable={}", pageable);

        return postRepository.findTopByViewCount(pageable)
                .map(this::convertToDto);
    }

    /**
     * PostEntity를 PostDto로 변환 (닉네임 포함)
     * @param postEntity 게시글 엔티티
     * @return 게시글 DTO
     */
    private PostDto convertToDto(PostEntity postEntity) {
        String nickname = userRepository.findById(postEntity.getUserId())
                .map(user -> user.getNickname())
                .orElse("unknown");

        return PostDto.from(postEntity, nickname);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPostsInBoard(Long boardId, String searchType, String keyword, Pageable pageable) {
        log.info("SEARCH IN BOARD: boardId={}, searchType={}, keyword={}", boardId, searchType, keyword);

        Page<PostEntity> entities = postRepository.findByBoardIdAndSearchType(boardId, searchType, keyword, pageable);

        return entities.map(this::convertToDto);
    }

    @Override
    public Page<PostDto> getMyPosts(Long userId, Pageable pageable) {
        log.info("내 게시글 조회: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        String nickname = userRepository.findById(userId)
                .map(UserEntity::getNickname)
                .orElse("알수없음");

        return postRepository.findByUserId(userId, pageable)
                .map(post -> PostDto.from(post, nickname));
    }
}