package com.polyHub.board.schedule.repository;

import com.polyHub.board.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // [추가]

// JpaRepository를 상속받는 것만으로도 기본적인 CRUD 메소드가 자동으로 생성됩니다.
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // [추가] 제목 또는 내용에 키워드가 포함된 일정을 검색하는 메소드
    List<Schedule> findByTitleContainingOrContentContaining(String title, String content);
}