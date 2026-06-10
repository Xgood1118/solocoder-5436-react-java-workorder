package com.workorder.service;

import com.workorder.model.WorkOrderLog;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.repository.WorkOrderLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkOrderLogService {

    private final WorkOrderLogRepository logRepository;

    public WorkOrderLogService(WorkOrderLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public WorkOrderLog log(String workOrderId, String operation, String operator,
                            WorkOrderStatus fromStatus, WorkOrderStatus toStatus, String remark) {
        WorkOrderLog log = new WorkOrderLog();
        log.setId(UUID.randomUUID().toString());
        log.setWorkOrderId(workOrderId);
        log.setOperation(operation);
        log.setOperator(operator);
        log.setOperationTime(LocalDateTime.now());
        log.setFromStatus(fromStatus != null ? fromStatus.name() : null);
        log.setToStatus(toStatus != null ? toStatus.name() : null);
        log.setRemark(remark);
        return logRepository.save(log);
    }

    public List<WorkOrderLog> getLogsByWorkOrderId(String workOrderId) {
        return logRepository.findByWorkOrderId(workOrderId);
    }

    public List<WorkOrderLog> getAllLogs() {
        return logRepository.findAll();
    }
}
