package com.workorder.controller;

import com.workorder.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/avg-handle-time")
    public Map<String, Object> getAverageHandleTime() {
        return statisticsService.getAverageHandleTime();
    }

    @GetMapping("/sla-rate")
    public Map<String, Object> getSlaAchievementRate() {
        return statisticsService.getSlaAchievementRate();
    }

    @GetMapping("/worker-load")
    public Map<String, Integer> getWorkerLoad() {
        return statisticsService.getWorkerLoad();
    }

    @GetMapping("/bottlenecks")
    public Map<String, Long> getBottleneckWorkstations() {
        return statisticsService.getBottleneckWorkstations();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        return statisticsService.getDashboardStats();
    }
}
