package com.polyHub.board.notice.controller;

import com.polyHub.board.notice.entity.Notice;
import com.polyHub.board.repository.NoticeRepository;
import com.polyHub.board.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;
    private final NoticeService noticeService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       Model model) {
        int pageSize = 10;
        Page<Notice> noticePage = noticeService.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("noticePage", noticePage);
        model.addAttribute("notices", noticePage.getContent()); // 🟩 추가: 실제 게시글 목록

        return "board/notice/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<Notice> noticeOpt = noticeRepository.findById(id);
        if (noticeOpt.isPresent()) {
            Notice notice = noticeOpt.get();
            // 조회수 증가
            notice.setView_cnt(notice.getView_cnt() + 1);
            noticeRepository.save(notice);

            model.addAttribute("notice", notice);
            return "board/notice/detail";
        } else {
            return "redirect:/notice";
        }
    }

    // 📌 공지사항 등록 페이지
    @GetMapping("/write")
    public String createForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "board/notice/write"; // /templates/notice/create.html
    }

    // 📌 공지사항 등록 처리
    @PostMapping("/write")
    public String create(@ModelAttribute Notice notice) {
        noticeRepository.save(notice);
        return "redirect:/notice";
    }

    // 📌 공지사항 수정 페이지
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Notice> noticeOpt = noticeRepository.findById(id);
        if (noticeOpt.isPresent()) {
            model.addAttribute("notice", noticeOpt.get());
            return "board/notice/edit";
        } else {
            return "redirect:/notice";
        }
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Notice notice) {
        Optional<Notice> existingOpt = noticeRepository.findById(id);
        if (existingOpt.isPresent()) {
            Notice existing = existingOpt.get();
            existing.setTitle(notice.getTitle());
            existing.setContent(notice.getContent());
            existing.setWriter(notice.getWriter());
            existing.setIsPinned(notice.getIsPinned());
            noticeRepository.save(existing);
        }
        return "redirect:/notice";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        noticeRepository.deleteById(id);
        return "redirect:/notice";
    }
}
