package com.likelion.team4.global.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.likelion.team4.global.response.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

    @Test
    void 커스텀예외_발생시_경고로그를_남기고_에러코드에_맞는_응답을_반환한다() {
        CustomException exception = new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);

        ResponseEntity<ApiResponse<?>> response = handler.handleCustomException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("E409");
        assertThat(logAppender.list)
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("E409"));
    }

    @Test
    void 처리되지_않은_예외_발생시_에러로그와_원본예외를_남기고_500_응답을_반환한다() {
        RuntimeException exception = new RuntimeException("예상치 못한 오류");

        ResponseEntity<ApiResponse<?>> response = handler.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(logAppender.list)
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getThrowableProxy() != null
                        && event.getThrowableProxy().getMessage().equals("예상치 못한 오류"));
    }
}
