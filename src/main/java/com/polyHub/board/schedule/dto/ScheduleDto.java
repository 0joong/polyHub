package com.polyHub.board.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private String title;   // FullCalendar의 'title' 속성
    private String start;   // FullCalendar의 'start' 속성 (예: "2025-06-12")
    private String end;     // FullCalendar의 'end' 속성 (예: "2025-06-13")
    private String content; // 추가적인 내용
}