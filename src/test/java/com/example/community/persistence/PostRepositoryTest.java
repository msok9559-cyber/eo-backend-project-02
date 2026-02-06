package com.example.community.persistence;

import com.example.community.domain.post.PostEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    private final Long BOARD_ID = 1L;
    private final Long USER_ID = 1L;

    @Test
    public void testExists() {
        assertNotNull(postRepository);

        log.info("postRepository = {}", postRepository);
    }

    @Test
    public void testGetList() {
        var postEntityList = postRepository.findAll();

        log.info("postEntityList.size() = {}", postEntityList.size());

        assertNotNull(postEntityList);
    }

    @Test
    public void testGetListWithPaging() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        Page<PostEntity> postEntityPage = postRepository.findAll(pageable);

        assertNotNull(postEntityPage);
        assertEquals(10, postEntityPage.getSize());
        assertEquals(0, postEntityPage.getNumber());

        log.info("Page.getTotalElements() = {}", postEntityPage.getTotalElements());
        log.info("Page.getTotalPages() = {}", postEntityPage.getTotalPages());
        log.info("Page.getNumber() = {}", postEntityPage.getNumber());
        log.info("Page.getSize() = {}", postEntityPage.getSize());
    }

    @Test
    public void testGetListByBoardWithPaging() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        Page<PostEntity> page = postRepository.findByBoardId(BOARD_ID, pageable);

        assertNotNull(page);
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertTrue(page.getContent().stream().allMatch(p -> p.getBoardId().equals(BOARD_ID)));

        log.info("boardId={} totalElements={}", BOARD_ID, page.getTotalElements());
    }

    @Test
    public void testCreate() {
        String title = "[TEST] PostRepositoryTest#testCreate";

        PostEntity postEntity = PostEntity.builder()
                .boardId(BOARD_ID)
                .userId(USER_ID)
                .title(title)
                .content(title)
                .postType((short) 0)
                .fixed((short) 0)
                .viewCount(0)
                .commentsCount(0)
                .likesCount(0)
                .build();

        log.info("postEntity(before save) = {}", postEntity);

        PostEntity savedEntity = postRepository.save(postEntity);

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(title, savedEntity.getTitle());

        log.info("savedEntity = {}", savedEntity);
    }

    @Test
    public void testRead() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .boardId(BOARD_ID)
                        .userId(USER_ID)
                        .title("[TEST] PostRepositoryTest#testRead")
                        .content("content")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );

        Long id = saved.getId();

        postRepository.findById(id).ifPresentOrElse(
                postEntity -> {
                    assertEquals(id, postEntity.getId());
                    log.info("postEntity = {}", postEntity);
                },
                () -> { throw new RuntimeException("읽을 게시글이 없습니다."); }
        );
    }

    @Test
    public void testUpdate() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .boardId(BOARD_ID)
                        .userId(USER_ID)
                        .title("[TEST] before update")
                        .content("before update")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );

        Long id = saved.getId();

        String newTitle = "[TEST] PostRepositoryTest#testUpdate";
        String newContent = "updated content";

        PostEntity postEntity = postRepository.findById(id).orElseThrow();
        log.info("postEntity(before) = {}", postEntity);

        postEntity.updateTitle(newTitle)
                .updateContent(newContent);

        PostEntity updatedEntity = postRepository.save(postEntity);

        assertNotNull(updatedEntity);
        assertEquals(newTitle, updatedEntity.getTitle());
        assertEquals(newContent, updatedEntity.getContent());

        log.info("updatedEntity = {}", updatedEntity);
    }

    @Test
    public void testDelete() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .boardId(BOARD_ID)
                        .userId(USER_ID)
                        .title("[TEST] PostRepositoryTest#testDelete")
                        .content("delete content")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .viewCount(0)
                        .commentsCount(0)
                        .likesCount(0)
                        .build()
        );

        Long id = saved.getId();

        final long countBefore = postRepository.count();
        log.info("countBefore = {}", countBefore);

        postRepository.findById(id).ifPresent(postEntity -> {
            postRepository.delete(postEntity);
            log.info("deletedEntity = {}", postEntity);

            final long countAfter = postRepository.count();
            log.info("countAfter = {}", countAfter);

            assertEquals(countBefore - 1, countAfter);
            assertTrue(postRepository.findById(id).isEmpty());
        });
    }
}
