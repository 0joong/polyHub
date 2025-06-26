package com.polyHub.board.survey.entity;

import com.polyHub.board.survey.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SURVEY_QUESTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // [추가] Builder를 위해 모든 필드를 사용하는 생성자 추가
@Builder            // [추가] 빌더 패턴을 사용할 수 있도록 어노테이션 추가
public class SurveyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SURVEY_QUESTION_SEQ_GEN")
    @SequenceGenerator(name = "SURVEY_QUESTION_SEQ_GEN", sequenceName = "SURVEY_QUESTION_SEQ", allocationSize = 1)
    private Long id;

    @Setter // [추가] 편의 메소드에서 사용하기 위해 Setter 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SURVEY_ID", nullable = false)
    private Survey survey;

    @Column(nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    private int questionOrder;
    private boolean isRequired;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)

    @Builder.Default // [수정] Builder가 이 필드를 new ArrayList<>()로 초기화하도록 설정
    private List<QuestionOption> options = new ArrayList<>();

    // --- [추가] 연관 관계 편의 메소드 ---
    public void addOption(QuestionOption option) {
        this.options.add(option);
        option.setQuestion(this); // 자식 엔티티에도 부모를 설정
    }
}