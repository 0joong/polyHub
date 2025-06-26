package com.polyHub.board.schedule.service;

import com.polyHub.board.schedule.dto.ScheduleDto;
import com.polyHub.board.schedule.entity.Schedule;
import com.polyHub.board.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    public List<ScheduleDto> findAll() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleDto create(ScheduleDto dto) {
        Schedule schedule = Schedule.builder()
                .title(dto.getTitle())
                .startDate(LocalDate.parse(dto.getStart()))
                .endDate((dto.getEnd() != null && !dto.getEnd().isEmpty()) ? LocalDate.parse(dto.getEnd()) : null)
                .content(dto.getContent())
                .build();
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleDto update(Long id, ScheduleDto dto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + id));
        schedule.update(dto);
        return convertToDto(schedule);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. id=" + id));
        scheduleRepository.delete(schedule);
    }

    private ScheduleDto convertToDto(Schedule schedule) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new ScheduleDto(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartDate().format(formatter),
                schedule.getEndDate() != null ? schedule.getEndDate().format(formatter) : null,
                schedule.getContent()
        );
    }
}