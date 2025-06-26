package com.polyHub.board.schedule.controller;

import com.polyHub.board.schedule.dto.ScheduleDto;
import com.polyHub.board.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어 의존성을 주입합니다.
public class ScheduleApiController {

    // Service 구현체(ScheduleServiceImpl)가 아닌, 인터페이스(ScheduleService)를 주입받습니다.
    private final ScheduleService scheduleService;

    /**
     * 모든 일정을 조회하는 API
     * @return 일정 목록 (JSON)
     */
    @GetMapping("/schedules")
    public List<ScheduleDto> getAllSchedules() {
        return scheduleService.findAll();
    }

    /**
     * 새 일정을 생성하는 API (관리자 전용)
     * @param scheduleDto 프론트에서 보낸 일정 데이터
     * @return 저장된 일정 정보
     */
    @PostMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ScheduleDto createSchedule(@RequestBody ScheduleDto scheduleDto) {
        return scheduleService.create(scheduleDto);
    }

    /**
     * 기존 일정을 수정하는 API (관리자 전용)
     * @param id 수정할 일정의 ID
     * @param scheduleDto 수정할 내용
     * @return 수정된 일정 정보
     */
    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDto scheduleDto) {
        try {
            ScheduleDto updatedDto = scheduleService.update(id, scheduleDto);
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 일정을 삭제하는 API (관리자 전용)
     * @param id 삭제할 일정의 ID
     * @return 성공/실패 응답
     */
    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
