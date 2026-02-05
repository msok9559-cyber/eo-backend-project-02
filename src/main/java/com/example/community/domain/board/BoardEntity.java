package com.example.community.domain.board;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boards")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "Board title is required")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public BoardEntity(String title) {
        this.title = title;
    }

    public BoardEntity updateTitle(String title) {
        this.title = title;
        return this;
    }

    public BoardEntity update(@NotNull BoardDto boardDto) {
        this.title = boardDto.getTitle();
        return this;
    }

    public static BoardEntity from(@NotNull BoardDto boardDto) {
        return BoardEntity.builder()
                .title(boardDto.getTitle())
                .build();
    }
}
