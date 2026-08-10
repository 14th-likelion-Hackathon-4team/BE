package com.likelion.team4.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "E400", "요청 값이 유효하지 않습니다"),

    // 인증 (auth)
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "E401", "아이디 또는 비밀번호가 일치하지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E401", "유효하지 않은 토큰입니다"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "E401", "Refresh Token이 유효하지 않습니다. 다시 로그인해주세요"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "E409", "이미 사용 중인 아이디입니다"),

    // 공통 리소스
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "요청한 리소스를 찾을 수 없습니다"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "E403", "해당 루틴에 접근할 권한이 없습니다"),

    // AI 대화
    ALREADY_PROCESSED_MISSION(HttpStatus.CONFLICT, "E409", "이미 처리된 미션입니다"),
    LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "E504", "미션 생성이 지연되고 있습니다. 잠시 후 다시 시도해주세요"),

    // 서버
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "서버 내부 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}