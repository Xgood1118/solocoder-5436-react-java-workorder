package com.workorder.service;

import com.workorder.model.WorkOrder;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import com.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final WorkOrderRepository workOrderRepository;

    public StatisticsService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public Map<String, Object> getAverageHandleTime() {
        Map<String, Object> result = new HashMap<>();
        List<WorkOrder> completedOrders = workOrderRepository.findByStatus(WorkOrderStatus.COMPLETED);

        Map<WorkOrderType, List<WorkOrder>> groupedByType = completedOrders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getType));

        for (Map.Entry<WorkOrderType, List<WorkOrder>> entry : groupedByType.entrySet()) {
            long totalMinutes = 0;
            int count = 0;

            for (WorkOrder wo : entry.getValue()) {
                if (wo.getCreatedAt() != null && wo.getCompletedAt() != null) {
                    long minutes = Duration.between(wo.getCreatedAt(), wo.getCompletedAt()).toMinutes();
                    minutes -= wo.getTotalSuspendedMinutes();
                    if (minutes > 0) {
                        totalMinutes += minutes;
                        count++;
                    }
                }
            }

            double avgMinutes = count > 0 ? (double) totalMinutes / count : 0;
            result.put(entry.getKey().name(), formatDuration(avgMinutes));
        }

        return result;
    }

    public Map<String, Object> getSlaAchievementRate() {
        Map<String, Object> result = new HashMap<>();
        List<WorkOrder> completedOrders = workOrderRepository.findByStatus(WorkOrderStatus.COMPLETED);

        Map<WorkOrderType, List<WorkOrder>> groupedByType = completedOrders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getType));

        for (Map.Entry<WorkOrderType, List<WorkOrder>> entry : groupedByType.entrySet()) {
            long totalWithSla = entry.getValue().stream()
                    .filter(wo -> wo.getSlaDeadline() != null)
                    .count();
            long breached = entry.getValue().stream()
                    .filter(WorkOrder::isSlaBreached)
                    .count();

            double rate = totalWithSla > 0 ? (double) (totalWithSla - breached) / totalWithSla * 100 : 100.0;
            result.put(entry.getKey().name(), String.format("%.2f%%", rate));
        }

        return result;
    }

    public Map<String, Integer> getWorkerLoad() {
        Map<String, Integer> result = new HashMap<>();
        List<WorkOrder> activeOrders = workOrderRepository.findByStatusIn(
                List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED,
                        WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.SUSPENDED)
        );

        for (WorkOrder wo : activeOrders) {
            if (wo.getAssignee() != null) {
                result.merge(wo.getAssignee(), 1, Integer::sum);
            }
        }

        return result;
    }

    public Map<String, Long> getBottleneckWorkstations() {
        List<WorkOrder> activeOrders = workOrderRepository.findByStatusIn(
                List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED,
                        WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.SUSPENDED)
        );

        Map<String, Long> workstationCounts = activeOrders.stream()
                .filter(wo -> wo.getWorkstation() != null)
                .collect(Collectors.groupingBy(WorkOrder::getWorkstation, Collectors.counting()));

        return workstationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = workOrderRepository.findAll().size();
        long createdCount = workOrderRepository.countByStatus(WorkOrderStatus.CREATED);
        long inProgressCount = workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS);
        long completedCount = workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED);
        long suspendedCount = workOrderRepository.countByStatus(WorkOrderStatus.SUSPENDED);
        long returnedCount = workOrderRepository.countByStatus(WorkOrderStatus.RETURNED);

        stats.put("total", totalOrders);
        stats.put("created", createdCount);
        stats.put("inProgress", inProgressCount);
        stats.put("completed", completedCount);
        stats.put("suspended", suspendedCount);
        stats.put("returned", returnedCount);
        stats.put("avgHandleTime", getAverageHandleTime());
        stats.put("slaRate", getSlaAchievementRate());
        stats.put("bottlenecks", getBottleneckWorkstations());

        return stats;
    }

    private String formatDuration(double totalMinutes) {
        long hours = (long) (totalMinutes / 60);
        long minutes = (long) (totalMinutes % 60);
        if (hours > 0) {
            return hours + "小时" + minutes + "分钟";
        }
        return minutes + "分钟";
    }
}
