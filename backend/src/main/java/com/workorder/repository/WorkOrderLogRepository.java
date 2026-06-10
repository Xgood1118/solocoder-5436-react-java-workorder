package com.workorder.repository;

import com.workorder.model.WorkOrderLog;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WorkOrderLogRepository {

    private final Map<String, WorkOrderLog> logs = new ConcurrentHashMap<>();

    public WorkOrderLog save(WorkOrderLog log) {
        logs.put(log.getId(), log);
        return log;
    }

    public List<WorkOrderLog> findByWorkOrderId(String workOrderId) {
        return logs.values().stream()
                .filter(log -> workOrderId.equals(log.getWorkOrderId()))
                .sorted(Comparator.comparing(WorkOrderLog::getOperationTime))
                .collect(Collectors.toList());
    }

    public List<WorkOrderLog> findAll() {
        return logs.values().stream()
                .sorted(Comparator.comparing(WorkOrderLog::getOperationTime).reversed())
                .collect(Collectors.toList());
    }
}
