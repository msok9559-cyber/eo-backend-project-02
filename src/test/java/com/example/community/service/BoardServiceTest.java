package com.example.community.service;

import com.example.community.domain.board.BoardDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
@Transactional
public class BoardServiceTest {
    @Autowired
    private BoardService boardService;

    @Test
    public void testCreate() {

        BoardDto dto = BoardDto.builder()
                .title("서비스 생성 테스트")
                .build();

        boardService.create(dto);

        assertThat(dto.getId()).isNotNull();
        log.info("생성된 게시판 ID: {}", dto.getId());
    }

    @Test
    public void testRead() {

        BoardDto dto = BoardDto.builder().title("서비스 조회 테스트").build();
        boardService.create(dto);
        Long id = dto.getId();

        Optional<BoardDto> found = boardService.read(id);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("서비스 조회 테스트");
        log.info("조회된 게시판 제목: {}", found.get().getTitle());
    }

    @Test
    public void testUpdate() {

        BoardDto dto = BoardDto.builder().title("수정 전 제목").build();
        boardService.create(dto);

        dto.setTitle("수정 후 제목");

        Optional<BoardDto> updated = boardService.update(dto);

        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("수정 후 제목");
        log.info("수정 완료: {}", updated.get().getTitle());
    }

    @Test
    public void testDelete() {

        BoardDto dto = BoardDto.builder().title("서비스 삭제 테스트").build();
        boardService.create(dto);
        Long id = dto.getId();

        boolean isDeleted = boardService.delete(id);

        assertThat(isDeleted).isTrue();
        assertThat(boardService.read(id)).isEmpty();
        log.info("삭제 여부: {}", isDeleted);
    }

    @Test
    public void testGetList() {

        boardService.create(BoardDto.builder().title("목록 테스트 1").build());
        boardService.create(BoardDto.builder().title("목록 테스트 2").build());

        List<BoardDto> list = boardService.getList();

        assertThat(list).isNotNull();
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
        log.info("전체 목록 개수: {}", list.size());
    }
}
