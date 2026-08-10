package com.likelion.team4.domain.main.controller;

import com.likelion.team4.domain.main.dto.MainResponse;
import com.likelion.team4.domain.main.dto.TodayNotificationResponse;
import com.likelion.team4.domain.main.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routinefit/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping
    public MainResponse getMainPage(
            @RequestParam Long userId
    ) {
        return mainService.getMainPage(userId);
    }

    @GetMapping("/notifications/today")
    public List<TodayNotificationResponse> getTodayNotifications(
            @RequestParam Long userId
    ) {
        return mainService.getTodayNotifications(userId);
    }
}