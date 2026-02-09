package com.example.community.persistence;

import com.example.community.domain.board.BoardEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    public void testSave() {

        BoardEntity board = BoardEntity.builder()
                .title("신규 게시판 테스트")
                .build();

        BoardEntity savedBoard = boardRepository.save(board);

        assertThat(savedBoard.getId()).isNotNull();
        assertThat(savedBoard.getTitle()).isEqualTo("신규 게시판 테스트");

        log.info("저장된 게시판: {}", savedBoard);
    }

    @Test
    public void testFindById() {

        BoardEntity saved = boardRepository.save(BoardEntity.builder().title("조회 테스트").build());

        Optional<BoardEntity> found = boardRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("조회 테스트");

        log.info("조회된 게시판: {}", found.get());
    }

    @Test
    public void testUpdate() {

        BoardEntity saved = boardRepository.save(BoardEntity.builder().title("수정 전").build());

        saved.updateTitle("수정 후");
        BoardEntity updated = boardRepository.save(saved);

        assertThat(updated.getTitle()).isEqualTo("수정 후");

        log.info("수정된 게시판: {}", updated);
    }

    @Test
    public void testDelete() {

        BoardEntity saved = boardRepository.save(BoardEntity.builder().title("삭제 테스트").build());
        Long id = saved.getId();

        boardRepository.delete(saved);

        Optional<BoardEntity> found = boardRepository.findById(id);
        assertThat(found).isEmpty();

        log.info("삭제 완료 ID: {}", id);
    }

    @Test
    public void testGetList() {

        List<BoardEntity> list = boardRepository.findAll();

        assertThat(list).isNotNull();
        log.info("전체 게시판 개수: {}", list.size());
    }
}