package com.polyHub.board.archive.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ARCHIVE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archive {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ARCHIVE_SEQ_GEN")
    @SequenceGenerator(name = "ARCHIVE_SEQ_GEN", sequenceName = "ARCHIVE_SEQ", allocationSize = 1)
    private Long id;

    private String title;
    @Lob private String content;
    private String writer;
    @Builder.Default private int viewCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // [수정] 자식 엔티티인 ArchiveFile과의 관계 설정
    @OneToMany(mappedBy = "archive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArchiveFile> archiveFiles = new ArrayList<>();

    // 연관관계 편의 메소드
    public void addFile(ArchiveFile archiveFile) {
        this.archiveFiles.add(archiveFile);
    }
}
