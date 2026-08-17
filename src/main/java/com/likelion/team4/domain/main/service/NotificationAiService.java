package com.likelion.team4.domain.main.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.team4.domain.routine.entity.Routine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationAiService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateNotification(Routine routine) {

        String prompt = String.format(
                """
                사용자가 실천해야 하는 루틴 정보를 바탕으로
                짧고 자연스러운 한국어 알림 문구를 작성해주세요.

                루틴명: %s
                실행 예정 시간: %s

                조건:
                - 사용자가 루틴을 실천하도록 부드럽게 독려해주세요.
                - 너무 압박하거나 죄책감을 주는 표현은 사용하지 마세요.
                - 30자 이내로 작성해주세요.
                - 알림 문구만 출력해주세요.
                """,
                routine.getTitle(),
                routine.getPerformTime()
        );

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(
                        "Authorization",
                        "Bearer " + openaiApiKey
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "max_tokens", 100,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        try {
            Map response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(10));

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return ((String) message.get("content")).trim();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI 알림 생성에 실패했습니다.",
                    e
            );
        }
    }
}