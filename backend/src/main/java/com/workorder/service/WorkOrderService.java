package com.workorder.service;

import com.workorder.exception.BusinessException;
import com.workorder.model.WorkOrder;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import com.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderLogService logService;

    public WorkOrderService(WorkOrderRepository workOrderRepository, WorkOrderLogService logService) {
        this.workOrderRepository = workOrderRepository;
        this.logService = logService;
    }

    public WorkOrder create(WorkOrder workOrder) {
        workOrder.setId(UUID.randomUUID().toString());
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setCreatedAt(LocalDateTime.now());
        if (workOrder.getType() == WorkOrderType.PRODUCTION) {
            workOrder.setCompletedQuantity(0);
        }
        workOrder.setTotalSuspendedMinutes(0);
        workOrder.setSlaBreached(false);

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(saved.getId(), "创建工单", workOrder.getCreator(),
                null, WorkOrderStatus.CREATED, workOrder.getDescription());
        return saved;
    }

    public WorkOrder createWithStatus(WorkOrder workOrder) {
        if (workOrder.getId() == null) {
            workOrder.setId(UUID.randomUUID().toString());
        }
        if (workOrder.getCreatedAt() == null) {
            workOrder.setCreatedAt(LocalDateTime.now());
        }
        if (workOrder.getType() == WorkOrderType.PRODUCTION && workOrder.getCompletedQuantity() == null) {
            workOrder.setCompletedQuantity(0);
        }

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(saved.getId(), "创建工单", workOrder.getCreator(),
                null, saved.getStatus(), workOrder.getDescription());
        return saved;
    }

    public WorkOrder getById(String id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工单不存在: " + id));
    }

    public List<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    public List<WorkOrder> getByType(WorkOrderType type) {
        return workOrderRepository.findByType(type);
    }

    public WorkOrder update(WorkOrder workOrder) {
        WorkOrder existing = getById(workOrder.getId());
        workOrder.setStatus(existing.getStatus());
        workOrder.setCreatedAt(existing.getCreatedAt());
        return workOrderRepository.save(workOrder);
    }

    public void delete(String id) {
        workOrderRepository.deleteById(id);
    }

    public WorkOrder assign(String workOrderId, String assignee, String operator) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.CREATED && fromStatus != WorkOrderStatus.RETURNED) {
            throw new BusinessException("只有已创建或已退回状态的工单才能分派，当前状态: " + fromStatus.getDescription());
        }

        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setAssignee(assignee);
        workOrder.setAssignedAt(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "分派工单", operator, fromStatus, WorkOrderStatus.ASSIGNED,
                "分派给: " + assignee);
        return saved;
    }

    public WorkOrder accept(String workOrderId, String operator) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.ASSIGNED) {
            throw new BusinessException("只有已分派状态的工单才能接单，当前状态: " + fromStatus.getDescription());
        }

        workOrder.setStatus(WorkOrderStatus.ACCEPTED);
        workOrder.setAcceptedAt(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "接单", operator, fromStatus, WorkOrderStatus.ACCEPTED, null);
        return saved;
    }

    public WorkOrder startProgress(String workOrderId, String operator) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.ACCEPTED && fromStatus != WorkOrderStatus.SUSPENDED) {
            throw new BusinessException("只有已接单或已挂起状态的工单才能开始处理，当前状态: " + fromStatus.getDescription());
        }

        if (fromStatus == WorkOrderStatus.SUSPENDED) {
            workOrder.setResumedAt(LocalDateTime.now());
            if (workOrder.getSuspendedAt() != null) {
                long suspendedMinutes = Duration.between(workOrder.getSuspendedAt(), LocalDateTime.now()).toMinutes();
                workOrder.setTotalSuspendedMinutes(workOrder.getTotalSuspendedMinutes() + suspendedMinutes);
            }
        }

        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        if (workOrder.getStartedAt() == null) {
            workOrder.setStartedAt(LocalDateTime.now());
        }

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "开始处理", operator, fromStatus, WorkOrderStatus.IN_PROGRESS, null);
        return saved;
    }

    public WorkOrder complete(String workOrderId, String operator, String remark) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.IN_PROGRESS) {
            throw new BusinessException("只有处理中状态的工单才能完成，当前状态: " + fromStatus.getDescription());
        }

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setCompletedAt(LocalDateTime.now());

        if (workOrder.getSlaDeadline() != null) {
            workOrder.setSlaBreached(LocalDateTime.now().isAfter(workOrder.getSlaDeadline()));
        }

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "完成工单", operator, fromStatus, WorkOrderStatus.COMPLETED, remark);
        return saved;
    }

    public WorkOrder returnOrder(String workOrderId, String operator, String returnReason) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.IN_PROGRESS && fromStatus != WorkOrderStatus.ACCEPTED) {
            throw new BusinessException("只有处理中或已接单状态的工单才能退回，当前状态: " + fromStatus.getDescription());
        }

        workOrder.setStatus(WorkOrderStatus.RETURNED);
        workOrder.setReturnedAt(LocalDateTime.now());
        workOrder.setReturnReason(returnReason);
        workOrder.setSlaDeadline(LocalDateTime.now().plusHours(4));
        workOrder.setSlaBreached(false);
        workOrder.setAssignee(null);
        workOrder.setAssignedAt(null);

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "退回工单", operator, fromStatus, WorkOrderStatus.RETURNED,
                "退回原因: " + returnReason + "，SLA：4小时");
        return saved;
    }

    public WorkOrder suspend(String workOrderId, String operator, String reason) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.IN_PROGRESS) {
            throw new BusinessException("只有处理中状态的工单才能挂起，当前状态: " + fromStatus.getDescription());
        }

        workOrder.setStatus(WorkOrderStatus.SUSPENDED);
        workOrder.setSuspendedAt(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "挂起工单", operator, fromStatus, WorkOrderStatus.SUSPENDED,
                "挂起原因: " + reason);
        return saved;
    }

    public WorkOrder resume(String workOrderId, String operator) {
        WorkOrder workOrder = getById(workOrderId);
        WorkOrderStatus fromStatus = workOrder.getStatus();

        if (fromStatus != WorkOrderStatus.SUSPENDED) {
            throw new BusinessException("只有已挂起状态的工单才能恢复，当前状态: " + fromStatus.getDescription());
        }

        return startProgress(workOrderId, operator);
    }

    public WorkOrder updateProgress(String workOrderId, int completedQuantity, String operator) {
        WorkOrder workOrder = getById(workOrderId);

        if (workOrder.getType() != WorkOrderType.PRODUCTION) {
            throw new BusinessException("只有生产工单才能更新进度数量");
        }

        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new BusinessException("只有处理中状态的工单才能更新进度");
        }

        workOrder.setCompletedQuantity(completedQuantity);

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "更新进度", operator, null, null,
                "已完成数量: " + completedQuantity + "/" + workOrder.getPlannedQuantity());
        return saved;
    }

    public List<WorkOrder> getEquipmentHistory(String equipmentCode) {
        return workOrderRepository.findByEquipmentCode(equipmentCode);
    }

    public List<WorkOrder> getQualityOrdersByProductionOrder(String productionOrderId) {
        return workOrderRepository.findBySourceProductionOrderId(productionOrderId);
    }

    public int escalateExpiredReturnedOrders() {
        List<WorkOrder> returnedOrders = workOrderRepository.findByStatus(WorkOrderStatus.RETURNED);
        int escalatedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (WorkOrder wo : returnedOrders) {
            if (wo.getSlaDeadline() != null
                    && now.isAfter(wo.getSlaDeadline())
                    && !wo.isSlaBreached()) {

                WorkOrderStatus fromStatus = wo.getStatus();
                wo.setStatus(WorkOrderStatus.CREATED);
                wo.setSlaBreached(true);
                wo.setAssignee(null);
                wo.setAssignedAt(null);
                wo.setAcceptedAt(null);
                wo.setStartedAt(null);
                workOrderRepository.save(wo);

                String upgradeTarget = wo.getCreator() != null ? wo.getCreator() : "车间主任";
                logService.log(wo.getId(), "SLA自动升级", "系统",
                        fromStatus, WorkOrderStatus.CREATED,
                        "退回工单超过4小时未处理，自动升级至: " + upgradeTarget);

                escalatedCount++;
            }
        }

        return escalatedCount;
    }
}
