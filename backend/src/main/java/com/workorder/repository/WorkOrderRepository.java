package com.workorder.repository;

import com.workorder.model.WorkOrder;
import com.workorder.model.enums.Priority;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WorkOrderRepository {

    private final Map<String, WorkOrder> workOrders = new ConcurrentHashMap<>();

    public WorkOrder save(WorkOrder workOrder) {
        workOrders.put(workOrder.getId(), workOrder);
        return workOrder;
    }

    public Optional<WorkOrder> findById(String id) {
        return Optional.ofNullable(workOrders.get(id));
    }

    public List<WorkOrder> findAll() {
        return new ArrayList<>(workOrders.values());
    }

    public List<WorkOrder> findByStatus(WorkOrderStatus status) {
        return workOrders.values().stream()
                .filter(wo -> wo.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findByType(WorkOrderType type) {
        return workOrders.values().stream()
                .filter(wo -> wo.getType() == type)
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findByAssignee(String assignee) {
        return workOrders.values().stream()
                .filter(wo -> assignee.equals(wo.getAssignee()))
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findByWorkstation(String workstation) {
        return workOrders.values().stream()
                .filter(wo -> workstation.equals(wo.getWorkstation()))
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findByStatusIn(List<WorkOrderStatus> statuses) {
        Set<WorkOrderStatus> statusSet = new HashSet<>(statuses);
        return workOrders.values().stream()
                .filter(wo -> statusSet.contains(wo.getStatus()))
                .sorted(Comparator.comparingInt((WorkOrder wo) -> wo.getPriority().getLevel()).reversed())
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findQueue() {
        return workOrders.values().stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.CREATED || wo.getStatus() == WorkOrderStatus.RETURNED)
                .sorted(Comparator.comparingInt((WorkOrder wo) -> wo.getPriority().getLevel()).reversed()
                        .thenComparing(WorkOrder::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findByEquipmentCode(String equipmentCode) {
        return workOrders.values().stream()
                .filter(wo -> WorkOrderType.EQUIPMENT.equals(wo.getType()) && equipmentCode.equals(wo.getEquipmentCode()))
                .sorted(Comparator.comparing(WorkOrder::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<WorkOrder> findBySourceProductionOrderId(String productionOrderId) {
        return workOrders.values().stream()
                .filter(wo -> WorkOrderType.QUALITY.equals(wo.getType()) && productionOrderId.equals(wo.getSourceProductionOrderId()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        workOrders.remove(id);
    }

    public long countByStatus(WorkOrderStatus status) {
        return workOrders.values().stream()
                .filter(wo -> wo.getStatus() == status)
                .count();
    }

    public long countByAssigneeAndStatusIn(String assignee, List<WorkOrderStatus> statuses) {
        Set<WorkOrderStatus> statusSet = new HashSet<>(statuses);
        return workOrders.values().stream()
                .filter(wo -> assignee.equals(wo.getAssignee()) && statusSet.contains(wo.getStatus()))
                .count();
    }
}
