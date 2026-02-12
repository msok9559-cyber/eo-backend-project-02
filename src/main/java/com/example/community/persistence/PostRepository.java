package com.example.community.persistence;

import com.example.community.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 게시판별 게시글 목록
    Page<PostEntity> findByBoardId(Long boardId, Pageable pageable);

    // 전체 게시물 목록 (메인 페이지용)
    Page<PostEntity> findAll(Pageable pageable);

    // 제목으로 검색
    @Query("SELECT p FROM PostEntity p WHERE p.title LIKE %:keyword%")
    Page<PostEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // 내용으로 검색
    @Query("SELECT p FROM PostEntity p WHERE p.content LIKE %:keyword%")
    Page<PostEntity> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    // 제목 + 내용 검색
    @Query("SELECT p FROM PostEntity p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<PostEntity> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

    // 작성자로 검색 (userId 기반)
    @Query("SELECT p FROM PostEntity p JOIN UserEntity u ON p.userId = u.id WHERE u.nickname LIKE %:keyword%")
    Page<PostEntity> searchByWriter(@Param("keyword") String keyword, Pageable pageable);

    // 조회수 TOP 10 (인기 게시글)
    @Query("SELECT p FROM PostEntity p ORDER BY p.viewCount DESC")
    Page<PostEntity> findTopByViewCount(Pageable pageable);

    @Query("SELECT p FROM PostEntity p WHERE p.boardId = :boardId AND (" +
            "(:searchType = 'title' AND p.title LIKE %:keyword%) OR " +
            "(:searchType = 'content' AND p.content LIKE %:keyword%) OR " +
            "(:searchType = 'writer' AND p.userId IN (SELECT u.id FROM UserEntity u WHERE u.nickname LIKE %:keyword%)) OR " +
            "(:searchType = 'titleContent' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)) OR " +
            "(:searchType = '' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)))")
    Page<PostEntity> findByBoardIdAndSearchType(
            @Param("boardId") Long boardId,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable);
}