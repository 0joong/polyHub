package com.polyHub.board.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 첨부파일의 간단한 정보(ID, 원본 이름)를 담는 DTO 입니다.
 * 주로 화면에 파일 목록을 보여줄 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private Long id;
    private String originalFileName;
}
