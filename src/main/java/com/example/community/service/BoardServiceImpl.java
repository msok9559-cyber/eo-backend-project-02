package com.example.community.service;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.board.BoardEntity;
import com.example.community.persistence.BoardRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public void create(@NotNull BoardDto boardDto) {
        log.info("CREATE: boardDto = {}", boardDto);

        // 테스트중 임시 주석
        // checkTitleAvailability(boardDto.getTitle());

        BoardEntity savedEntity = boardRepository.save(BoardEntity.from(boardDto));

        boardDto.setId(savedEntity.getId());
    }

    @Override
    public Optional<BoardDto> read(@NotNull Long id) {
        log.info("READ: id = {}", id);

        return boardRepository.findById(id).map(BoardDto::from);
    }

    @Override
    public Optional<BoardDto> update(@NotNull BoardDto boardDto) {
        log.info("UPDATE: boardDto = {}", boardDto);

        return boardRepository.findById(boardDto.getId()).map(boardEntity -> {

            String newTitle = boardDto.getTitle();
            String oldTitle = boardEntity.getTitle();

            // 제목이 바뀐 경우에만 중복 체크(임시로 주석처리 테스트중)
//            if (!oldTitle.equals(newTitle)) {
//                checkTitleAvailability(newTitle);
//            }

            BoardEntity updatedEntity = boardRepository.save(boardEntity.update(boardDto));
            return BoardDto.from(updatedEntity);
        });
    }

    @Override
    public boolean delete(@NotNull Long id) {
        log.info("DELETE: id = {}", id);
        return boardRepository.findById(id).map(boardEntity -> {
            boardRepository.delete(boardEntity);
            return true;
        }).orElse(false);
    }

    @Override
    public List<BoardDto> getList() {
        log.info("GET LIST: All Boards");

        return boardRepository.findAll().stream()
                .map(BoardDto::from)
                .collect(Collectors.toList());
    }

    private void checkTitleAvailability(@NotNull String title) {
        if (boardRepository.existsByTitle(title)) {
            throw new IllegalArgumentException("Board title is already in use");
        }
    }
}
