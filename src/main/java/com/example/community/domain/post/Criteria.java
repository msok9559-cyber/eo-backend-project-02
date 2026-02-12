package com.example.community.domain.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 페이지 정보를 전달하기 위한 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Criteria {
    private int page = 1;
    private int size = 10;
    private String searchType;
    private String keyword;
}
