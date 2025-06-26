package com.polyHub.board.survey.service;

import com.polyHub.board.survey.dto.*;
import com.polyHub.board.survey.entity.*;
import com.polyHub.board.survey.enums.QuestionType;
import com.polyHub.board.survey.enums.SurveyStatus;
import com.polyHub.board.survey.repository.*;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyParticipationRepository participationRepository;
    private final SurveyAnswerRepository answerRepository;
    private final SurveyQuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final MemberRepository memberRepository;

    /**
     * [추가] 설문을 강제로 마감하는 로직 구현
     */
    @Override
    @Transactional
    public void closeSurvey(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));
        survey.setStatus(SurveyStatus.CLOSED);
        // @Transactional에 의해 메소드 종료 시 변경 사항이 DB에 반영됩니다.
    }


    /**
     * [수정] 새 설문 생성 시 시작 날짜를 확인하여 바로 ACTIVE 상태로 만들 수 있도록 수정합니다.
     */
    @Override
    @Transactional
    public Long writeSurvey(SurveyCreationDto creationDto, String creator) {
        LocalDate startDate = LocalDate.parse(creationDto.getStartDate());
        LocalDate today = LocalDate.now();

        // 시작일이 오늘이거나 과거이면 ACTIVE, 미래이면 READY 상태로 설정
        SurveyStatus initialStatus = (startDate.isBefore(today) || startDate.isEqual(today))
                ? SurveyStatus.ACTIVE
                : SurveyStatus.READY;

        Survey survey = Survey.builder()
                .title(creationDto.getTitle())
                .description(creationDto.getDescription())
                .startDate(startDate)
                .endDate(LocalDate.parse(creationDto.getEndDate()))
                .createdBy(creator)
                .status(initialStatus) // [수정] 계산된 초기 상태를 설정
                .build();

        int questionOrder = 1;
        for (SurveyCreationDto.QuestionCreationDto qDto : creationDto.getQuestions()) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .questionText(qDto.getQuestionText())
                    .questionType(qDto.getQuestionType())
                    .isRequired(qDto.isRequired())
                    .questionOrder(questionOrder++)
                    .build();

            if (qDto.getQuestionType() != QuestionType.TEXT && qDto.getOptions() != null) {
                int optionOrder = 1;
                for (String optionText : qDto.getOptions()) {
                    QuestionOption option = QuestionOption.builder()
                            .optionText(optionText)
                            .optionOrder(optionOrder++)
                            .build();
                    question.addOption(option);
                }
            }
            survey.addQuestion(question);
        }

        Survey savedSurvey = surveyRepository.save(survey);
        return savedSurvey.getId();
    }

    @Override
    public Page<SurveyDto> findSurveysByStatus(SurveyStatus status, Pageable pageable) {
        Page<Survey> surveys = surveyRepository.findByStatus(status, pageable);
        return surveys.map(this::convertToSurveyDtoSimple);
    }

    // --- [수정] findSurveyForParticipation 메소드의 데이터 조회 방식을 완전히 새로운 로직으로 교체합니다. ---
    @Override
    public SurveyDto findSurveyForParticipation(Long surveyId) {
        // 1. Survey와 그에 속한 Question 목록을 Fetch Join으로 한 번에 가져옵니다.
        Survey survey = surveyRepository.findByIdWithDetails(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));

        // 2. 가져온 Question들의 ID만 모두 추출합니다.
        List<Long> questionIds = survey.getQuestions().stream()
                .map(SurveyQuestion::getId)
                .collect(Collectors.toList());

        // 3. Question ID 목록을 사용하여, 관련된 모든 Option들을 별도의 쿼리로 한 번에 가져옵니다.
        List<QuestionOption> allOptions = Collections.emptyList();
        if (!questionIds.isEmpty()) {
            allOptions = optionRepository.findAllByQuestionIdIn(questionIds);
        }

        // 4. 서비스 로직 안에서 Question과 Option을 수동으로 조합하기 위해,
        //    Question ID를 Key로 하는 Option 리스트 맵을 생성합니다.
        Map<Long, List<QuestionOption>> optionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));

        // 5. 수동으로 조합된 데이터를 기반으로 최종 DTO를 생성합니다.
        return convertToSurveyDtoWithManuallyMappedOptions(survey, optionsMap);
    }

    /**
     * [추가] 수동으로 매핑된 데이터를 DTO로 변환하는 새로운 헬퍼 메소드
     */
    private SurveyDto convertToSurveyDtoWithManuallyMappedOptions(Survey survey, Map<Long, List<QuestionOption>> optionsMap) {
        List<QuestionDto> questionDtos = survey.getQuestions().stream()
                .map(question -> {
                    List<QuestionOption> optionsForThisQuestion = optionsMap.getOrDefault(question.getId(), Collections.emptyList());

                    List<OptionDto> optionDtos = optionsForThisQuestion.stream()
                            .map(this::convertToOptionDto)
                            .collect(Collectors.toList());

                    return QuestionDto.builder()
                            .id(question.getId())
                            .questionText(question.getQuestionText())
                            .questionType(question.getQuestionType())
                            .questionOrder(question.getQuestionOrder())
                            .required(question.isRequired())
                            .options(optionDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .status(survey.getStatus())
                .questions(questionDtos)
                .build();
    }

    /**
     * [최종] 사용자가 제출한 설문 답변을 저장하는 완전한 로직입니다.
     * MultiValueMap을 사용하여 다중 선택 답변을 올바르게 처리합니다.
     */
    @Override
    @Transactional
    public void submitSurvey(MultiValueMap<String, String> allParams, Long respondentId) {
        Long surveyId = Long.parseLong(allParams.getFirst("surveyId"));

        // 1. 중복 참여 확인
        if (participationRepository.findBySurveyIdAndRespondentId(surveyId, respondentId).isPresent()) {
            throw new IllegalStateException("이미 참여한 설문입니다.");
        }

        // 2. 참여 정보 생성
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));
        Member respondent = memberRepository.findById(respondentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        SurveyParticipation participation = SurveyParticipation.builder()
                .survey(survey)
                .respondent(respondent)
                .build();
        SurveyParticipation savedParticipation = participationRepository.save(participation);

        // 3. 답변 저장
        for (Map.Entry<String, List<String>> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("answers[")) {
                Long questionId = Long.parseLong(key.substring(key.indexOf('[') + 1, key.indexOf(']')));
                SurveyQuestion question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));

                // entry.getValue()는 값의 '리스트'이므로 순회하며 저장
                for (String value : entry.getValue()) {
                    SurveyAnswer.SurveyAnswerBuilder answerBuilder = SurveyAnswer.builder()
                            .participation(savedParticipation)
                            .question(question);

                    if (question.getQuestionType() == QuestionType.TEXT) {
                        answerBuilder.textResponse(value);
                    } else {
                        Long optionId = Long.parseLong(value);
                        QuestionOption option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선택지입니다."));
                        answerBuilder.selectedOption(option);
                    }
                    answerRepository.save(answerBuilder.build());
                }
            }
        }
    }


    private SurveyDto convertToSurveyDtoSimple(Survey survey) {
        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .status(survey.getStatus())
                .build();
    }

    private SurveyDto convertToSurveyDtoWithQuestions(Survey survey) {
        List<QuestionDto> questionDtos = survey.getQuestions().stream()
                .map(this::convertToQuestionDto) // 이 부분에서 오류 발생
                .collect(Collectors.toList());

        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .status(survey.getStatus())
                .questions(questionDtos)
                .build();
    }

    // [수정] 이 메소드가 클래스 내에 **단 한 번만** 존재하도록 확인합니다.
    private QuestionDto convertToQuestionDto(SurveyQuestion question) {
        List<OptionDto> optionDtos = question.getOptions().stream()
                .map(this::convertToOptionDto)
                .collect(Collectors.toList());

        return QuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .questionOrder(question.getQuestionOrder())
                .required(question.isRequired())
                .options(optionDtos)
                .build();
    }

    private OptionDto convertToOptionDto(QuestionOption option) {
        return OptionDto.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .optionOrder(option.getOptionOrder())
                .build();
    }

    public void deleteSurvey(Long surveyId) {
        // Survey 엔티티의 cascade 설정에 의해 연관된 모든 데이터(질문, 선택지, 참여정보, 답변)가
        // 한 번에 함께 삭제됩니다.
        surveyRepository.deleteById(surveyId);
    }

    @Override
    public SurveyResultDto getSurveyResult(Long surveyId) {
        // 1. 설문 기본 정보 조회
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));

        // 2. 총 참여자 수 조회
        long totalParticipants = participationRepository.countBySurveyId(surveyId);

        // 3. 각 질문별로 결과를 가공하여 DTO 리스트 생성
        List<SurveyResultDto.QuestionResultDto> questionResults = survey.getQuestions().stream()
                .map(question -> {
                    // 3-1. 현재 질문에 대한 모든 답변을 DB에서 가져옴
                    List<SurveyAnswer> answers = answerRepository.findAllByQuestionId(question.getId());

                    // 3-2. 질문 유형에 따라 다른 방식으로 결과 DTO를 생성
                    if (question.getQuestionType() == QuestionType.TEXT) {
                        // 주관식: 답변 텍스트 목록을 생성
                        List<String> textAnswers = answers.stream()
                                .map(SurveyAnswer::getTextResponse)
                                .filter(text -> text != null && !text.isBlank()) // 비어있지 않은 답변만 필터링
                                .collect(Collectors.toList());

                        return SurveyResultDto.QuestionResultDto.builder()
                                .questionText(question.getQuestionText())
                                .questionType(question.getQuestionType())
                                .textAnswers(textAnswers)
                                .optionCounts(Collections.emptyMap()) // null 대신 빈 Map으로 초기화
                                .build();

                    } else {
                        // 객관식: 선택지별 응답 수를 계산
                        Map<String, Long> optionCounts = answers.stream()
                                .filter(a -> a.getSelectedOption() != null)
                                .collect(Collectors.groupingBy(
                                        a -> a.getSelectedOption().getOptionText(),
                                        Collectors.counting()
                                ));

                        return SurveyResultDto.QuestionResultDto.builder()
                                .questionText(question.getQuestionText())
                                .questionType(question.getQuestionType())
                                .optionCounts(optionCounts)
                                .textAnswers(Collections.emptyList()) // null 대신 빈 List로 초기화
                                .build();
                    }
                })
                .collect(Collectors.toList());

        // 4. 최종 결과 DTO를 조립하여 반환
        return SurveyResultDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .totalParticipants(totalParticipants)
                .questions(questionResults)
                .build();
    }

    /**
     * [추가] 설문 상태를 자동으로 업데이트하는 스케줄링 메소드.
     * 매일 자정에 실행됩니다.
     */
    @Override
    @Transactional // [주의] 데이터를 변경하는 작업이므로 @Transactional이 반드시 필요합니다.
    @Scheduled(cron = "0 0 0 * * *") // 매일 0시 0분 0초에 실행
    public void updateSurveyStatus() {
        LocalDate today = LocalDate.now();

        int updatedToActive = surveyRepository.updateReadyToActive(today);
        int updatedToClosed = surveyRepository.updateActiveToClosed(today);

        // (선택사항) 스케줄러 실행 로그
        System.out.println("Survey status updated: " + updatedToActive + " surveys activated, " + updatedToClosed + " surveys closed.");
    }
}
