package com.polyHub.board.archive.controller;

import com.polyHub.board.archive.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Toast UI 에디터에서 이미지를 업로드할 때 사용되는 API
     * @param imageFile 업로드된 이미지 파일
     * @return 저장된 이미지의 URL을 담은 JSON 응답
     */
    @PostMapping("/image-upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            String imageUrl = fileStorageService.storeImage(imageFile);
            // Toast UI Editor는 {"url": "..."} 형태의 JSON을 기대합니다.
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}