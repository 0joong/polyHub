package com.polyHub.board.schedule.repository;

import com.polyHub.board.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository를 상속받는 것만으로도 기본적인 CRUD 메소드가 자동으로 생성됩니다.
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
