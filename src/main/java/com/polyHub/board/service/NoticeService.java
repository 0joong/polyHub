package com.polyHub.board.service;

import com.polyHub.board.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NoticeService {

    List<Notice> findAll();

    Page<Notice> findAll(Pageable pageable);

    Optional<Notice> findById(Long id);

    Notice save(Notice notice);

    void deleteById(Long id);

    void increaseViews(Long id);
}
