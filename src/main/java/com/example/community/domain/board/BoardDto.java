package com.example.community.domain.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDto {
    private Long id;

    @NotBlank(message = "Board title is required")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    private String title;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardDto from(@NotNull BoardEntity boardEntity) {
        return BoardDto.builder()
                .id(boardEntity.getId())
                .title(boardEntity.getTitle())
                .createdAt(boardEntity.getCreatedAt())
                .updatedAt(boardEntity.getUpdatedAt())
                .build();
    }
}
