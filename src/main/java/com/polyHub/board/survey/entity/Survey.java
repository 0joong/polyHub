package com.polyHub.board.survey.entity;

import com.polyHub.board.survey.enums.SurveyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SURVEY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SURVEY_SEQ_GEN")
    @SequenceGenerator(name = "SURVEY_SEQ_GEN", sequenceName = "SURVEY_SEQ", allocationSize = 1)
    private Long id;

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SurveyStatus status = SurveyStatus.READY;

    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // [수정] Survey는 Question 목록만 가져야 합니다.
    // 잘못 추가된 options 리스트가 있었다면 이 부분에서 제거됩니다.
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyQuestion> questions = new ArrayList<>();

    // 연관 관계 편의 메소드
    public void addQuestion(SurveyQuestion question) {
        this.questions.add(question);
        question.setSurvey(this);
    }
}