package com.workorder.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderLog {
    private String id;
    private String workOrderId;
    private String operation;
    private String operator;
    private LocalDateTime operationTime;
    private String remark;
    private String fromStatus;
    private String toStatus;
}
