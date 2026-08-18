package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationAiService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.openai.com")
            .build();

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
            Map response = restClient.post()
                    .uri("/v1/chat/completions")
                    .header(
                            "Authorization",
                            "Bearer " + openaiApiKey
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return ((String) message.get("content")).trim();

        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.AI_NOTIFICATION_GENERATION_FAILED
            );
        }
    }
}