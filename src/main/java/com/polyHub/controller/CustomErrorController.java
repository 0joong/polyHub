package com.polyHub.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // HTTP 에러 코드를 가져옵니다.
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "common/under-construction";
            }
            // 500 (Internal Server Error) 에러일 경우
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // 500 에러를 위한 전용 페이지로 연결합니다.
                return "common/error/error-500";
            }
        }
        return "common/error/access-denied";
    }
}