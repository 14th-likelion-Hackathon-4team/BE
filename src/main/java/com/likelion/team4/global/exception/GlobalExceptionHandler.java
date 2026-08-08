package com.likelion.team4.global.exception;

import com.likelion.team4.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), null);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "reason", fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_INPUT.getCode(),
                ErrorCode.INVALID_INPUT.getMessage(),
                errors
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                null
        );
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
    }
}
