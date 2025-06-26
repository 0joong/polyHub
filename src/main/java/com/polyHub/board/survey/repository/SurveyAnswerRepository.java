package com.polyHub.board.survey.repository;

import com.polyHub.board.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    /**
     * [추가] 특정 질문 ID에 해당하는 모든 답변 목록을 조회합니다.
     * @param questionId 조회할 질문의 ID
     * @return 해당 질문에 대한 모든 답변 리스트
     */
    List<SurveyAnswer> findAllByQuestionId(Long questionId);
}