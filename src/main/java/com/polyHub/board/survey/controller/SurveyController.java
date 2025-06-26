package com.polyHub.board.survey.controller;

import com.polyHub.board.survey.dto.SurveyCreationDto;
import com.polyHub.board.survey.dto.SurveyDto;
import com.polyHub.board.survey.dto.SurveyResultDto;
import com.polyHub.board.survey.enums.SurveyStatus;
import com.polyHub.board.survey.service.SurveyService;
import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 설문 목록 페이지를 보여줍니다.
     * @param status 'ACTIVE' 또는 'CLOSED' 상태 (기본값: ACTIVE)
     * @param pageable 페이징 정보
     * @param model 뷰에 데이터를 전달할 모델
     * @return 설문 목록 뷰
     */
    @GetMapping
    public String surveyList(
            @RequestParam(defaultValue = "ACTIVE") SurveyStatus status,
            @PageableDefault(size = 9, sort = "endDate", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        model.addAttribute("surveys", surveyService.findSurveysByStatus(status, pageable));
        model.addAttribute("currentStatus", status); // 현재 선택된 탭을 활성화하기 위해
        return "board/survey/list";
    }

    /**
     * 설문 참여 페이지를 보여줍니다.
     * @param id 참여할 설문의 ID
     * @param model 뷰에 데이터를 전달할 모델
     * @return 설문 참여 뷰
     */
    @GetMapping("/{id}")
    public String surveyDetail(@PathVariable Long id, Model model) {
        // TODO: 이미 참여한 사용자인지, 설문 기간이 맞는지 등 추가적인 검증 로직 필요
        SurveyDto survey = surveyService.findSurveyForParticipation(id);

        model.addAttribute("survey", survey);
        return "board/survey/detail";
    }

    /**
     * [최종 수정] 사용자가 제출한 설문 답변을 처리하고, JSON으로 결과를 반환합니다.
     * @param allParams 모든 폼 데이터
     * @param currentUser 현재 로그인한 사용자
     * @return 성공 또는 실패 메시지를 담은 JSON 응답
     */
    @PostMapping("/submit")
    @ResponseBody // 이 어노테이션을 통해 반환값이 JSON 데이터가 됩니다.
    public ResponseEntity<Map<String, String>> submitSurvey(
            @RequestParam MultiValueMap<String, String> allParams,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails // CustomUserDetails를 사용하신다면 해당 타입으로 변경
    ) {
        if (currentUserDetails == null) {
            // 로그인되지 않은 사용자의 접근
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        // 2. CustomUserDetail에서 실제 Member 객체 가져오기
        Member currentUser = currentUserDetails.getMember();

        try {
            surveyService.submitSurvey(allParams, currentUser.getId());
            // 성공 시
            return ResponseEntity.ok(Map.of("message", "설문 제출이 완료되었습니다."));
        } catch (IllegalStateException e) {
            // 이미 참여한 경우 등의 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "이미 참여한 설문입니다."));
        } catch (Exception e) {
            // 기타 예외 처리
            e.printStackTrace(); // 서버 로그에 실제 오류를 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * (관리자용) 설문 생성 폼 페이지를 보여줍니다.
     */
    @GetMapping("/write")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createSurveyForm() {
        return "board/survey/write";
    }

    /**
     * (관리자용) 새 설문 생성을 처리합니다.
     * 참고: 동적으로 질문/선택지를 추가하는 복잡한 폼은 JavaScript로 처리하는 것이 일반적입니다.
     */
    @PostMapping("/write")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String writeSurvey(@ModelAttribute SurveyCreationDto creationDto) {
        // TODO: 현재 로그인한 사용자 정보 가져와서 두 번째 인자로 넘기기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long newSurveyId = surveyService.writeSurvey(creationDto, username);
        // [수정 후] 방금 만든 설문의 상세 페이지로 리다이렉트
        return "redirect:/survey/" + newSurveyId;
    }

    /**
     * (관리자용) 설문 삭제를 처리합니다.
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return "redirect:/survey";
    }

    /**
     * [추가] 설문 결과 보기 페이지를 보여줍니다.
     * @param id 결과를 조회할 설문의 ID
     * @param model 뷰에 데이터를 전달할 모델
     * @return 설문 결과 뷰 이름
     */
    @GetMapping("/result/{id}")
    public String surveyResult(@PathVariable Long id, Model model) {
        // TODO: 설문이 마감되지 않았거나, 비공개 설문일 경우 접근을 제어하는 로직 추가 가능

        // 1. 서비스에서 통계 처리된 결과 데이터를 가져옵니다.
        SurveyResultDto resultDto = surveyService.getSurveyResult(id);

        // 2. 모델에 'result'라는 이름으로 결과 데이터를 담습니다.
        model.addAttribute("result", resultDto);

        // 3. 'board/survey/result.html' 뷰를 반환합니다.
        return "board/survey/result";
    }

    /**
     * [추가] (관리자용) 설문 강제 마감을 처리합니다.
     */
    @PostMapping("/close/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String closeSurvey(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        surveyService.closeSurvey(id);
        redirectAttributes.addFlashAttribute("successMessage", "설문이 성공적으로 마감 처리되었습니다.");
        return "redirect:/survey"; // 마감 후 목록 페이지로 이동
    }

    // TODO: 설문 수정 기능 추가 (create와 유사)
}
