package com.polyHub.board.survey.repository;

import com.polyHub.board.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    // 현재는 기본 CRUD 기능으로 충분합니다.
}