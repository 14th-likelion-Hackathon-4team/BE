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