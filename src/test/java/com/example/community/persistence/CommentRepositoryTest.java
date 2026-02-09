package com.example.community.persistence;

import com.example.community.domain.comment.CommentEntity;
import com.example.community.domain.post.PostEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private final Long BOARD_ID = 1L;
    private final Long USER_ID = 1L;

    private Long postId;

    /* -------------------------------------------------
     * 공통 게시글 생성
     * ------------------------------------------------- */
    @BeforeEach
    void setUp() {
        PostEntity post = postRepository.save(
                PostEntity.builder()
                        .boardId(BOARD_ID)
                        .userId(USER_ID)
                        .title("[TEST] 게시글")
                        .content("게시글 내용")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );

        this.postId = post.getId();
    }

    @Test
    @DisplayName("CommentRepository 주입 테스트")
    void testExists() {
        assertThat(commentRepository).isNotNull();
        log.info("commentRepository = {}", commentRepository);
    }

    @Test
    @Transactional
    void testCreate() {
        PostEntity post = postRepository.findById(postId).orElseThrow();

        commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("안녕하세요")
                        .build()
        );

        commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("반갑습니다")
                        .build()
        );

        List<CommentEntity> comments =
                commentRepository.findByPostEntity(post);

        assertThat(comments).hasSize(2);

        comments.forEach(c ->
                log.info("댓글 ID={}, 내용={}", c.getId(), c.getContent())
        );
    }

    @Test
    @Transactional
    void testRead() {
        PostEntity post = postRepository.findById(postId).orElseThrow();

        CommentEntity saved = commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("안녕하세요")
                        .build()
        );

        CommentEntity found =
                commentRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getContent()).isEqualTo("안녕하세요");
        assertThat(found.getPostEntity().getId()).isEqualTo(postId);

        log.info("조회된 댓글 = {}", found.getContent());
    }

    @Test
    @Transactional
    void testUpdate() {
        PostEntity post = postRepository.findById(postId).orElseThrow();

        CommentEntity saved = commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("수정 전 댓글")
                        .build()
        );

        saved.updateContent("수정 후 댓글");
        CommentEntity updated = commentRepository.save(saved);

        assertThat(updated.getContent()).isEqualTo("수정 후 댓글");

        log.info("수정된 댓글 = {}", updated.getContent());
    }

    @Test
    @Transactional
    void testDelete() {
        PostEntity post = postRepository.findById(postId).orElseThrow();

        CommentEntity saved = commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("삭제될 댓글")
                        .build()
        );

        long countBefore = commentRepository.count();

        commentRepository.delete(saved);

        long countAfter = commentRepository.count();

        assertThat(countBefore - countAfter).isEqualTo(1);
    }

    @Test
    @Transactional
    void testGetList() {
        PostEntity post = postRepository.findById(postId).orElseThrow();

        commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("첫 번째 댓글")
                        .build()
        );

        commentRepository.save(
                CommentEntity.builder()
                        .userId(USER_ID)
                        .postEntity(post)
                        .content("두 번째 댓글")
                        .build()
        );

        List<CommentEntity> list =
                commentRepository.findByPostEntity(post);

        assertThat(list).hasSize(2);

        list.forEach(c ->
                log.info("댓글 내용 = {}", c.getContent())
        );
    }
}
