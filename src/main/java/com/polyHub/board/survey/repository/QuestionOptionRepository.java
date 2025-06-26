package com.polyHub.board.survey.repository;

import com.polyHub.board.survey.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    /**
     * [추가] 주어진 여러 개의 질문 ID에 해당하는 모든 선택지를 한 번의 쿼리로 조회합니다.
     * @param questionIds 질문 ID 목록
     * @return 조회된 선택지 목록
     */
    @Query("SELECT o FROM QuestionOption o WHERE o.question.id IN :questionIds")
    List<QuestionOption> findAllByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}
