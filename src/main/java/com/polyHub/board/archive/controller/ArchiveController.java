package com.polyHub.board.archive.controller;

import com.polyHub.board.archive.dto.ArchiveDto;
import com.polyHub.board.archive.entity.ArchiveFile;
import com.polyHub.board.archive.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public String list(
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, // 카드형이므로 size=9로 가정
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        model.addAttribute("archives", archiveService.findArchives(pageable, keyword));
        model.addAttribute("keyword", keyword); // 검색어 유지를 위해 모델에 추가

        return "board/archive/list"; // list.html 파일이 있는 실제 뷰 경로
    }

    // 2. 자료실 상세 보기 페이지
    @GetMapping("/{id}")
    public String archiveDetail(@PathVariable Long id, Model model) {
        ArchiveDto archive = archiveService.findArchiveById(id);
        model.addAttribute("archive", archive);
        return "board/archive/detail";
    }

    // 3. 자료실 글쓰기 페이지 (폼)
    @GetMapping("/write")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String writeForm() {
        return "board/archive/write";
    }

    // 4. 자료실 글쓰기 처리
    @PostMapping("/write")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String writeProcess(@ModelAttribute ArchiveDto archiveDto,
                               @RequestParam("newFiles") List<MultipartFile> files) throws Exception {
        // TODO: 현재 로그인한 사용자 정보를 가져와서 writer에 설정해야 합니다.
        archiveDto.setWriter(SecurityContextHolder.getContext().getAuthentication().getName());
        archiveService.save(archiveDto, files);
        return "redirect:/archive";
    }

    // 5. 자료실 수정 페이지 (폼)
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        ArchiveDto archive = archiveService.findArchiveById(id); // 수정 시에는 조회수 증가 안하는 메소드 사용 권장
        model.addAttribute("archive", archive);
        return "board/archive/edit";
    }

    // 6. 자료실 수정 처리
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String editProcess(@PathVariable Long id,
                              @ModelAttribute ArchiveDto archiveDto,
                              @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds,
                              @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) throws Exception {
        archiveService.update(id, archiveDto, deleteFileIds, newFiles);
        return "redirect:/archive/" + id;
    }

    // 7. 자료실 삭제 처리
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteProcess(@PathVariable Long id) {
        archiveService.delete(id);
        return "redirect:/archive";
    }

    // TODO: 8. 파일 다운로드 처리
    /**
     * 첨부파일 다운로드를 처리합니다.
     * @param fileId 다운로드할 파일의 ID
     * @return 실제 파일 데이터가 담긴 ResponseEntity
     * @throws MalformedURLException 파일 경로가 잘못된 경우 발생
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        // 1. 서비스로부터 파일 정보를 가져옵니다.
        ArchiveFile file = archiveService.findFileById(fileId);

        // 2. 서버에 저장된 파일의 전체 경로를 만듭니다.
        Path filePath = Paths.get(uploadDir).resolve(file.getStoredFileName()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        // 3. 파일이 실제로 존재하는지 확인합니다.
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + file.getStoredFileName());
        }

        // 4. 브라우저가 파일을 다운로드하도록 HTTP 헤더를 설정합니다.
        //    - 한글 파일명이 깨지지 않도록 인코딩합니다.
        String encodedOriginalFileName = URLEncoder.encode(file.getOriginalFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
