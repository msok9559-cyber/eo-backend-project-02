package com.example.community.persistence;

import com.example.community.domain.comment.CommentEntity;
import com.example.community.domain.post.PostEntity;
import com.example.community.domain.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@SpringBootTest
@Slf4j
public class CommentRepositoryTest {


    @Autowired
    private CommentRepository commentRepository;



    @Autowired
    private PostRepository postRepository;


    @Test
    @DisplayName("CommentRepository 주입 테스트")
    public void testExists() {
        assertThat(commentRepository).isNotNull();

        log.info("commentRepository = {}", commentRepository);
    }

    @BeforeEach
    void setUp() {
        PostEntity postEntity = PostEntity.builder()
                .title("test title")
                .content("test content")
                .boardId(1L)
                .userId(2L)
                .viewCount(3)
                .likesCount(5)
                .commentsCount(3)
                .build();

    }

    @Test
    @Transactional
    public void testCreate() {

        Optional<PostEntity> postEntityOptional = postRepository.findById(1L);
        assertThat(postEntityOptional).isNotEmpty();

        PostEntity postEntity = postEntityOptional.get();

        for (int i = 1; i <= 5; i++) {
            CommentEntity commentEntity = CommentEntity.builder()
                    .postEntity(postEntity)
                    .content("[TEST] CommentRepositoryTest#testCreate #" + i)
                    .build();

            CommentEntity savedEntity = commentRepository.save(commentEntity);
            log.info("savedEntity = {}", savedEntity);
        }

        List<CommentEntity> commentEntityList = postEntity.getCommentEntityList();
        assertThat(commentEntityList.size()).isEqualTo( 5);
    }

    @Test
    public void testRead() {
        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(1L);
        assertThat(commentEntityOptional).isNotEmpty();

        log.info("commentEntity = {}", commentEntityOptional.get());
    }

    @Test
    public void testUpdate() {
        final String content = "[TEST] CommentRepositoryTest#testUpdate";

        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(2L);
        assertThat(commentEntityOptional).isNotEmpty();

        CommentEntity commentEntity = commentEntityOptional.get()
                .updateContent(content);

        CommentEntity savedEntity = commentRepository.save(commentEntity);

        // assertThat(savedEntity.getContent()).isEqualTo(content);
        assertThat(savedEntity).returns(content, from(CommentEntity::getContent));
        log.info("savedEntity = {}", savedEntity);
    }

    @Test
    public void testDelete() {
        final long countBefore = commentRepository.count();

        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(2L);
        assertThat(commentEntityOptional).isNotEmpty();

        CommentEntity commentEntity = commentEntityOptional.get();

        commentRepository.delete(commentEntity);

        final long countAfter = commentRepository.count();
        assertThat(countBefore - countAfter).isEqualTo(1);

        log.info("deletedEntity = {}", commentEntity);
        log.info("deleted records = {}", countBefore - countAfter);
    }

    @Test
    public void testGetList() {
        Optional<PostEntity> postEntityOptional = postRepository.findById(1L);
        assertThat(postEntityOptional).isNotEmpty();

        PostEntity postEntity = postEntityOptional.get();

        List<CommentEntity> commentEntityList = commentRepository.findByPostEntity(postEntity);

        assertThat(commentEntityList).isNotEmpty();
        log.info("commentEntityList.size() = {}", commentEntityList.size());
    }

    @Test
    public void testGetList2() {
        List<CommentEntity> commentEntityList = commentRepository.findByPostEntityId(1L);

        assertThat(commentEntityList).isNotEmpty();
        log.info("commentEntityList.size() = {}", commentEntityList.size());
    }
}
