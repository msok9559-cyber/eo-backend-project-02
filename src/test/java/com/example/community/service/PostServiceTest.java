package com.example.community.service;

import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class PostServiceTest {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

//    @Autowired
//    private UserRepository userRepository;

    private PostEntity anyPost() {
        return postRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("posts 더미데이터가 없습니다."));
    }

    @Test
    public void testExists() {
        assertNotNull(postService);
        log.info("postService = {}", postService);
    }

    @Test
    public void testCreate() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto postDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testCreate")
                .content("[TEST] PostServiceTest#testCreate")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        log.info("postDto(before) = {}", postDto);

        Long createdId = postService.create(boardId, postDto, userId);

        log.info("createdId = {}", createdId);
        log.info("postDto(after) = {}", postDto);

        assertNotNull(createdId);
        assertThat(createdId).isGreaterThan(0L);
    }

    @Test
    public void testRead() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testRead")
                .content("read content")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        int before = postRepository.findById(postId).orElseThrow().getViewCount();

        PostDto postDto = postService.read(postId);

        assertNotNull(postDto);
        log.info("postDto = {}", postDto);

        int after = postRepository.findById(postId).orElseThrow().getViewCount();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    public void testUpdate() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] before update")
                .content("before update")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        PostDto updateDto = PostDto.builder()
                .id(postId)
                .title("[TEST] PostServiceTest#testUpdate")
                .content("update content")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        log.info("updateDto = {}", updateDto);

        //작성자 수정
        boolean result = postService.update(updateDto, userId);

        // DB 확인
        assertTrue(result);

        PostEntity updated = postRepository.findById(postId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("[TEST] PostServiceTest#testUpdate");
        assertThat(updated.getContent()).isEqualTo("update content");

    }


    @Test
    // 글 작성자 삭제 성공 테스트
    public void testDelete_owner_success() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] delete owner")
                .content("delete owner")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        boolean result = postService.delete(postId, userId);

        assertTrue(result);
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    public void testDelete_notOwner_fail() {
        Long boardId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        Long postId = postService.create(boardId,
                PostDto.builder().title("t").content("c").postType((short)0).fixed((short)0).build(),
                ownerId);

        boolean result = postService.delete(postId, otherUserId);

        assertFalse(result);
        assertThat(postRepository.findById(postId)).isPresent();
    }
}
