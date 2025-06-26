package com.polyHub.board.archive.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // application.properties에 설정된 파일 저장 경로
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeImage(MultipartFile file) throws Exception {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 파일 이름 유효성 검사
        if (originalFileName.contains("..")) {
            throw new Exception("잘못된 파일 경로입니다: " + originalFileName);
        }

        // 고유한 파일 이름 생성
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path targetLocation = Paths.get(uploadDir + File.separator + "images");

        // 이미지 저장 디렉토리가 없으면 생성
        if (!Files.exists(targetLocation)) {
            Files.createDirectories(targetLocation);
        }

        Path filePath = targetLocation.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath);

        // 브라우저에서 접근 가능한 URL 반환
        return "/images/" + storedFileName;
    }
}