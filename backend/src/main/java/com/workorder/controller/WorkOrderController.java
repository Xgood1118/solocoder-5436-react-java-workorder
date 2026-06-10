package com.workorder.controller;

import com.workorder.model.WorkOrder;
import com.workorder.model.enums.WorkOrderType;
import com.workorder.service.WorkOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public WorkOrder create(@RequestBody WorkOrder workOrder) {
        return workOrderService.create(workOrder);
    }

    @GetMapping
    public List<WorkOrder> getAll(@RequestParam(required = false) WorkOrderType type) {
        if (type != null) {
            return workOrderService.getByType(type);
        }
        return workOrderService.getAll();
    }

    @GetMapping("/{id}")
    public WorkOrder getById(@PathVariable String id) {
        return workOrderService.getById(id);
    }

    @PutMapping("/{id}")
    public WorkOrder update(@PathVariable String id, @RequestBody WorkOrder workOrder) {
        workOrder.setId(id);
        return workOrderService.update(workOrder);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        workOrderService.delete(id);
    }

    @PostMapping("/{id}/assign")
    public WorkOrder assign(@PathVariable String id, @RequestBody Map<String, String> body) {
        String assignee = body.get("assignee");
        String operator = body.getOrDefault("operator", "system");
        return workOrderService.assign(id, assignee, operator);
    }

    @PostMapping("/{id}/accept")
    public WorkOrder accept(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        return workOrderService.accept(id, operator);
    }

    @PostMapping("/{id}/start")
    public WorkOrder start(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        return workOrderService.startProgress(id, operator);
    }

    @PostMapping("/{id}/complete")
    public WorkOrder complete(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        String remark = body != null ? body.get("remark") : null;
        return workOrderService.complete(id, operator, remark);
    }

    @PostMapping("/{id}/return")
    public WorkOrder returnOrder(@PathVariable String id, @RequestBody Map<String, String> body) {
        String operator = body.getOrDefault("operator", "system");
        String reason = body.get("reason");
        return workOrderService.returnOrder(id, operator, reason);
    }

    @PostMapping("/{id}/suspend")
    public WorkOrder suspend(@PathVariable String id, @RequestBody Map<String, String> body) {
        String operator = body.getOrDefault("operator", "system");
        String reason = body.get("reason");
        return workOrderService.suspend(id, operator, reason);
    }

    @PostMapping("/{id}/resume")
    public WorkOrder resume(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        return workOrderService.resume(id, operator);
    }

    @PostMapping("/{id}/progress")
    public WorkOrder updateProgress(@PathVariable String id, @RequestBody Map<String, Object> body) {
        int completedQuantity = (Integer) body.get("completedQuantity");
        String operator = body.getOrDefault("operator", "system").toString();
        return workOrderService.updateProgress(id, completedQuantity, operator);
    }

    @GetMapping("/equipment/{equipmentCode}/history")
    public List<WorkOrder> getEquipmentHistory(@PathVariable String equipmentCode) {
        return workOrderService.getEquipmentHistory(equipmentCode);
    }

    @GetMapping("/production/{productionOrderId}/quality")
    public List<WorkOrder> getQualityOrdersByProductionOrder(@PathVariable String productionOrderId) {
        return workOrderService.getQualityOrdersByProductionOrder(productionOrderId);
    }
}
