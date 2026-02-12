package com.example.community.service;

import com.example.community.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    /**
     * 게시글 작성
     *
     * @param boardId 게시판 ID
     * @param postDto 게시글 정보
     * @param userId 로그인 사용자 ID
     * @return 생성된 게시글 ID
     */
    Long create (Long boardId, PostDto postDto, Long userId);

    /**
     * 게시글 조회 (조회수 증가 포함)
     * @param id 게시글 ID
     * @return 조회한 게시글 정보
     */
    PostDto read(Long id);

    /**
     * 게시글 수정 (작성자만 가능)
     * @param postDto 수정할 게시글 정보
     * @param userId 로그인 사용자 ID
     * @return 수정 성공 여부
     */
    boolean update(PostDto postDto, Long userId);

    /**
     * 게시글 삭제 (작성자, 관리자 가능)
     * @param id 게시글 ID
     * @param userId 로그인 사용자 ID
     * @return 삭제 성공 여부
     */
    boolean delete(Long id, Long userId);

    /**
     * 게시판별로 게시글 목록 조회
     * @param boardId 게시판 ID
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<PostDto> getList(Long boardId, Pageable pageable);

    /**
     * 전체 게시글 목록 조회
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<PostDto> getAllPosts(Pageable pageable);

    /**
     * 게시글 검색
     * @param searchType 검색 타입 (title, content, writer, titleContent)
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostDto> searchPosts(String searchType, String keyword, Pageable pageable);

    /**
     * 인기 게시글 조회 (조회수 TOP N)
     * @param pageable 페이징 정보
     * @return 인기 게시글 페이지
     */
    Page<PostDto> getPopularPosts(Pageable pageable);

    Page<PostDto> searchPostsInBoard(Long boardId, String searchType, String keyword, Pageable pageable);


}