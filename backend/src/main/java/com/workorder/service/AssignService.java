package com.workorder.service;

import com.workorder.exception.BusinessException;
import com.workorder.model.AssignRule;
import com.workorder.model.WorkOrder;
import com.workorder.model.Worker;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import com.workorder.repository.AssignRuleRepository;
import com.workorder.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkerService workerService;
    private final AssignRuleRepository assignRuleRepository;
    private final WorkOrderLogService logService;

    @Value("${workorder.max-worker-load:3}")
    private int maxWorkerLoad;

    public AssignService(WorkOrderRepository workOrderRepository, WorkerService workerService,
                         AssignRuleRepository assignRuleRepository, WorkOrderLogService logService) {
        this.workOrderRepository = workOrderRepository;
        this.workerService = workerService;
        this.assignRuleRepository = assignRuleRepository;
        this.logService = logService;
    }

    public boolean canAssignToWorker(String workerId) {
        long currentLoad = workOrderRepository.countByAssigneeAndStatusIn(
                workerId,
                List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED,
                        WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.SUSPENDED)
        );
        return currentLoad < maxWorkerLoad;
    }

    public int getWorkerCurrentLoad(String workerId) {
        return (int) workOrderRepository.countByAssigneeAndStatusIn(
                workerId,
                List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED,
                        WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.SUSPENDED)
        );
    }

    public WorkOrder manualAssign(String workOrderId, String workerId, String operator) {
        if (!canAssignToWorker(workerId)) {
            throw new BusinessException("工人当前负载已满，无法分派");
        }

        Worker worker = workerService.getById(workerId);
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在: " + workOrderId));

        WorkOrderStatus fromStatus = workOrder.getStatus();
        if (fromStatus != WorkOrderStatus.CREATED && fromStatus != WorkOrderStatus.RETURNED) {
            throw new BusinessException("只有已创建或已退回状态的工单才能分派");
        }

        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setAssignee(workerId);
        workOrder.setAssignedAt(java.time.LocalDateTime.now());
        if (workOrder.getWorkstation() == null) {
            workOrder.setWorkstation(worker.getWorkstation());
        }

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "手动分派", operator, fromStatus, WorkOrderStatus.ASSIGNED,
                "分派给工人: " + worker.getName());
        return saved;
    }

    public WorkOrder autoAssign(String workOrderId, String operator) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在: " + workOrderId));

        WorkOrderStatus fromStatus = workOrder.getStatus();
        if (fromStatus != WorkOrderStatus.CREATED && fromStatus != WorkOrderStatus.RETURNED) {
            throw new BusinessException("只有已创建或已退回状态的工单才能分派");
        }

        List<Worker> candidates = findCandidates(workOrder);
        if (candidates.isEmpty()) {
            throw new BusinessException("没有符合条件的工人可分派");
        }

        Worker bestWorker = candidates.get(0);

        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setAssignee(bestWorker.getId());
        workOrder.setAssignedAt(java.time.LocalDateTime.now());
        workOrder.setWorkstation(bestWorker.getWorkstation());

        WorkOrder saved = workOrderRepository.save(workOrder);
        logService.log(workOrderId, "自动分派", operator, fromStatus, WorkOrderStatus.ASSIGNED,
                "自动分派给工人: " + bestWorker.getName());
        return saved;
    }

    private List<Worker> findCandidates(WorkOrder workOrder) {
        AssignRule rule = assignRuleRepository.findEnabled().orElse(null);
        List<Worker> workers = workerService.getAll();

        return workers.stream()
                .filter(w -> canAssignToWorker(w.getId()))
                .filter(w -> {
                    if (rule != null && rule.isConsiderWorkstation() && workOrder.getWorkstation() != null) {
                        return workOrder.getWorkstation().equals(w.getWorkstation());
                    }
                    return true;
                })
                .filter(w -> {
                    if (rule != null && rule.isConsiderSkills()) {
                        String requiredSkill = getRequiredSkill(workOrder.getType());
                        return w.getSkills() != null && w.getSkills().contains(requiredSkill);
                    }
                    return true;
                })
                .sorted(Comparator.comparingInt(w -> getWorkerCurrentLoad(w.getId())))
                .collect(Collectors.toList());
    }

    private String getRequiredSkill(WorkOrderType type) {
        return switch (type) {
            case PRODUCTION -> "production";
            case QUALITY -> "quality";
            case EQUIPMENT -> "maintenance";
            case MATERIAL -> "material";
            case MOLD -> "mold";
        };
    }

    public AssignRule createRule(AssignRule rule) {
        rule.setId(UUID.randomUUID().toString());
        return assignRuleRepository.save(rule);
    }

    public AssignRule updateRule(AssignRule rule) {
        return assignRuleRepository.save(rule);
    }

    public List<AssignRule> getAllRules() {
        return assignRuleRepository.findAll();
    }

    public Optional<AssignRule> getEnabledRule() {
        return assignRuleRepository.findEnabled();
    }
}
