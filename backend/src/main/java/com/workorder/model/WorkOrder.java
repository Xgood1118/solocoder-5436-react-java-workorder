package com.workorder.model;

import com.workorder.model.enums.Priority;
import com.workorder.model.enums.QualityProcessType;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrder {
    private String id;
    private String title;
    private WorkOrderType type;
    private WorkOrderStatus status;
    private Priority priority;
    private String description;
    private String creator;
    private String assignee;
    private String workstation;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime returnedAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime resumedAt;
    private long totalSuspendedMinutes;
    private String returnReason;
    private LocalDateTime slaDeadline;
    private boolean slaBreached;
    private String sourceProductionOrderId;

    private Integer plannedQuantity;
    private Integer completedQuantity;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private String orderNumber;
    private String productionLine;
    private String operator;

    private String batchNumber;
    private String defectItem;
    private String responsibleProcess;
    private QualityProcessType qualityProcessType;

    private String equipmentCode;
    private String faultDescription;

    private String materialCode;
    private Integer requiredQuantity;

    private String moldCode;
    private LocalDateTime lastMaintenanceTime;
}
