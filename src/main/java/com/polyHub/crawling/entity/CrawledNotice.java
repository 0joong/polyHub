package com.polyHub.crawling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CRAWLED_NOTICE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrawledNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CRAWLED_NOTICE_SEQ_GEN")
    @SequenceGenerator(name = "CRAWLED_NOTICE_SEQ_GEN", sequenceName = "CRAWLED_NOTICE_SEQ", allocationSize = 1)
    private Long id;

    @Column(length = 50)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob // CLOB 타입과 매핑
    private String content;

    @Column(nullable = false, length = 512)
    private String noticeUrl;

    private LocalDate noticeDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

}