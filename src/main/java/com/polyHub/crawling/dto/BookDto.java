package com.polyHub.crawling.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookDto {
    private String title;
    private String author;
    private String status;
    private String publisher; // [추가] 출판사 필드
}
