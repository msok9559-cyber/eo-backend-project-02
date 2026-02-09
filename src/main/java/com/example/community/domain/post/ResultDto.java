package com.example.community.domain.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private boolean success;
    private String action;
    private String message;

    // 정적 팩토리 메서드(static factory methods)
    public static ResultDto of(boolean success, String action, String message) {
        return new ResultDto(success, action, message);
    }

    public static ResultDto of(boolean success, String action) {
        return new ResultDto(success, action, null);
    }
}
