package com.polyHub.board.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SURVEY_ANSWER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // [추가] Builder를 위해 모든 필드를 사용하는 생성자 추가
@Builder            // [추가] 빌더 패턴을 사용할 수 있도록 어노테이션 추가
public class SurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SURVEY_ANSWER_SEQ_GEN")
    @SequenceGenerator(name = "SURVEY_ANSWER_SEQ_GEN", sequenceName = "SURVEY_ANSWER_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTICIPATION_ID", nullable = false)
    private SurveyParticipation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUESTION_ID", nullable = false)
    private SurveyQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SELECTED_OPTION_ID") // 주관식일 경우 null
    private QuestionOption selectedOption;

    @Lob // CLOB 타입과 매핑
    private String textResponse; // 주관식 답변 내용
}