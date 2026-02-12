package com.example.community.service;

import com.example.community.domain.comment.CommentDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.CommentRepository;
import com.example.community.persistence.PostRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@Slf4j
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long postId;
    private final Long USER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    /* =====================
       테스트용 게시글 생성
     ===================== */
    @BeforeEach
    void setUp() {
        PostEntity post = postRepository.save(
                PostEntity.builder()
                        .boardId(1L)
                        .userId(USER_ID)
                        .title("테스트 게시글")
                        .content("게시글 내용")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );

        postId = post.getId();
    }

    @Test
    public void testExists(){
        assertNotNull(commentService);
        log.info("commentService = {}", commentService);
    }

    /* =====================
       댓글 생성 테스트
     ===================== */
    @Test
    public void testCreate() {
        CommentDto dto = CommentDto.builder()
                .postId(postId)
                .content("안녕하세요")
                .build();

        Optional<CommentDto> result = commentService.create(dto, USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("안녕하세요");
        assertThat(result.get().getPostId()).isEqualTo(postId);
    }

    @Test
    public void testCreateFail() {
        CommentDto dto = CommentDto.builder()
                .postId(postId)
                .content("댓글 생성 실패")
                .build();

        Optional<CommentDto> result = commentService.create(dto, null);

        assertThat(result).isEmpty();
    }

    /* =====================
       댓글 조회 테스트
     ===================== */
    @Test
    public void testRead() {
        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("반갑습니다")
                        .build(),
                USER_ID
        ).orElseThrow();

        Optional<CommentDto> read = commentService.read(created.getId());

        assertThat(read).isPresent();
        assertThat(read.get().getContent()).isEqualTo("반갑습니다");
    }

    /* =====================
       댓글 수정 테스트
     ===================== */
    @Test
    public void testUpdate() {
        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("수정 전")
                        .build(),
                USER_ID
        ).orElseThrow();

        created.setContent("수정 후");

        Optional<CommentDto> updated =
                commentService.update(created, USER_ID);

        assertThat(updated).isPresent();
        assertThat(updated.get().getContent()).isEqualTo("수정 후");
    }

    @Test
    public void testUpdateFail() {
        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("원본")
                        .build(),
                USER_ID
        ).orElseThrow();

        created.setContent("해킹 시도");

        Optional<CommentDto> updated =
                commentService.update(created, OTHER_USER_ID);

        assertThat(updated).isEmpty();
    }

    /* =====================
       댓글 삭제 테스트
     ===================== */
    @Test
    public void testDelete() {
        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("삭제 대상")
                        .build(),
                USER_ID
        ).orElseThrow();

        boolean deleted = commentService.delete(created.getId(), USER_ID);

        assertThat(deleted).isTrue();
        assertThat(commentRepository.findById(created.getId())).isEmpty();
    }

    /* =====================
       댓글 목록 조회 테스트
     ===================== */
    @Test
    public void testGetList() {
        commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("안녕하세요")
                        .build(),
                USER_ID
        );

        commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("반갑습니다")
                        .build(),
                USER_ID
        );

        List<CommentDto> list = commentService.getList(postId);

        assertThat(list).hasSize(2);
        assertThat(list)
                .extracting(CommentDto::getContent)
                .containsExactly("안녕하세요", "반갑습니다");
    }
}
