package com.polyHub.board.archive.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ARCHIVE_FILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveFile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ARCHIVE_FILE_SEQ_GEN")
    @SequenceGenerator(name = "ARCHIVE_FILE_SEQ_GEN", sequenceName = "ARCHIVE_FILE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @JoinColumn(name = "ARCHIVE_ID")
    private Archive archive; // 부모 게시글

    private String originalFileName;
    private String storedFileName;
    private long fileSize;

    @Builder
    public ArchiveFile(Archive archive, String originalFileName, String storedFileName, long fileSize) {
        this.archive = archive;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
    }
}