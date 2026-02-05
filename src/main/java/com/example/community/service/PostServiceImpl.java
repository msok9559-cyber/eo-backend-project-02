package com.example.community.service;

import com.example.community.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class PostServiceImpl implements PostService {
    @Override
    public Long create(Long boardId, PostDto postDto, Long loginUserId) {
        return 0L;
    }

    @Override
    public PostDto read(Long id) {
        return null;
    }

    @Override
    public boolean update(PostDto postDto, Long loginUserId) {
        return false;
    }

    @Override
    public boolean delete(Long id, Long loginUserId) {
        return false;
    }

    @Override
    public Page<PostDto> getList(Long boardId, Pageable pageable) {
        return null;
    }
}
