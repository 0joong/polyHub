package com.polyHub.board.survey.entity;

import com.polyHub.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SURVEY_PARTICIPATION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"SURVEY_ID", "RESPONDENT_ID"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SurveyParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SURVEY_PARTICIPATION_SEQ_GEN")
    @SequenceGenerator(name = "SURVEY_PARTICIPATION_SEQ_GEN", sequenceName = "SURVEY_PARTICIPATION_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SURVEY_ID", nullable = false)
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESPONDENT_ID", nullable = false)
    private Member respondent; // 사용자(응답자)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers = new ArrayList<>();
}
