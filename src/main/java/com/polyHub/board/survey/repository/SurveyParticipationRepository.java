package com.polyHub.board.survey.repository;

import com.polyHub.board.survey.entity.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, Long> {

    /**
     * 특정 설문(surveyId)에 특정 사용자(respondentId)가 참여했는지 확인합니다.
     * @param surveyId 설문 ID
     * @param respondentId 응답자(사용자) ID
     * @return 참여 정보 (존재하면 Optional.of, 없으면 Optional.empty)
     */
    Optional<SurveyParticipation> findBySurveyIdAndRespondentId(Long surveyId, Long respondentId);

    // [추가] 특정 설문에 참여한 총 인원 수를 조회합니다.
    long countBySurveyId(Long surveyId);
}