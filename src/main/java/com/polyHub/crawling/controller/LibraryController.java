package com.polyHub.crawling.controller;

import com.polyHub.crawling.dto.BookDto;
import com.polyHub.crawling.service.LibraryCrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryCrawlingService libraryCrawlingService;

    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam("query") String query) {
        List<BookDto> books = libraryCrawlingService.searchBooks(query);
        return ResponseEntity.ok(books);
    }
}
