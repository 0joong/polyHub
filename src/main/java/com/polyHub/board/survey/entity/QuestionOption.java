package com.polyHub.board.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "QUESTION_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // [추가] Builder를 위해 모든 필드를 사용하는 생성자 추가
@Builder            // [추가] 빌더 패턴을 사용할 수 있도록 어노테이션 추가
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QUESTION_OPTION_SEQ_GEN")
    @SequenceGenerator(name = "QUESTION_OPTION_SEQ_GEN", sequenceName = "QUESTION_OPTION_SEQ", allocationSize = 1)
    private Long id;

    @Setter // [추가] 편의 메소드에서 사용하기 위해 Setter 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUESTION_ID", nullable = false)
    private SurveyQuestion question;

    private String optionText;
    private int optionOrder;
}