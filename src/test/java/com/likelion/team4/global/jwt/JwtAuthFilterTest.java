package com.likelion.team4.global.jwt;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        logger = (Logger) LoggerFactory.getLogger(JwtAuthFilter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        SecurityContextHolder.clearContext();
    }

    @Test
    void 토큰검증_실패시_경고로그를_남기고_인증없이_필터체인을_계속_진행한다() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtil.validateToken("invalid-token"))
                .thenThrow(new RuntimeException("토큰 파싱 실패"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        assertThat(logAppender.list)
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("토큰 파싱 실패"));
    }

    @Test
    void 유효한_토큰이면_인증정보를_SecurityContext에_저장한다() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserId("valid-token")).thenReturn(1L);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
        verify(filterChain).doFilter(request, response);
        assertThat(logAppender.list).noneMatch(event -> event.getLevel() == Level.WARN);
    }
}
