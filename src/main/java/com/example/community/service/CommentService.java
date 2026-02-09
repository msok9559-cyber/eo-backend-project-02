package com.example.community.service;


import com.example.community.domain.comment.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Optional<CommentDto> create(CommentDto commentDto, Long userId);
    Optional<CommentDto> read(Long id);
    Optional<CommentDto> update(CommentDto commentDto, Long userId);
    boolean delete(Long id, Long userId);
    List<CommentDto> getList(Long postId);
}

