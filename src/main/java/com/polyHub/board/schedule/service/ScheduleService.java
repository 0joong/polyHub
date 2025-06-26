package com.polyHub.board.schedule.service;

import com.polyHub.board.schedule.dto.ScheduleDto;

import java.util.List;

public interface ScheduleService {

    List<ScheduleDto> findAll();

    ScheduleDto create(ScheduleDto dto);

    ScheduleDto update(Long id, ScheduleDto dto);

    void delete(Long id);
}
