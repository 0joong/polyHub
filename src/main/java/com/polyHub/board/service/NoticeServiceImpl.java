package com.polyHub.board.service;

import com.polyHub.board.notice.entity.Notice;
import com.polyHub.board.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> findAll() {
        return noticeRepository.findAll();
    }

    @Override
    public Page<Notice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    public Optional<Notice> findById(Long id) {
        return noticeRepository.findById(id);
    }

    @Transactional
    public Notice save(Notice notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteById(Long id) {
        noticeRepository.deleteById(id);
    }

    @Transactional
    public void increaseViews(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다. ID: " + id));
        notice.setView_cnt(notice.getView_cnt() + 1);
        noticeRepository.save(notice);
    }
}
