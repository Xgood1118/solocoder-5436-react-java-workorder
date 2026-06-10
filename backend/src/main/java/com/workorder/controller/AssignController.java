package com.workorder.controller;

import com.workorder.model.AssignRule;
import com.workorder.model.WorkOrder;
import com.workorder.service.AssignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assign")
public class AssignController {

    private final AssignService assignService;

    public AssignController(AssignService assignService) {
        this.assignService = assignService;
    }

    @PostMapping("/manual/{workOrderId}/{workerId}")
    public WorkOrder manualAssign(@PathVariable String workOrderId,
                                  @PathVariable String workerId,
                                  @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        return assignService.manualAssign(workOrderId, workerId, operator);
    }

    @PostMapping("/auto/{workOrderId}")
    public WorkOrder autoAssign(@PathVariable String workOrderId,
                                @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.getOrDefault("operator", "system") : "system";
        return assignService.autoAssign(workOrderId, operator);
    }

    @GetMapping("/can-assign/{workerId}")
    public Map<String, Boolean> canAssign(@PathVariable String workerId) {
        return Map.of("canAssign", assignService.canAssignToWorker(workerId));
    }

    @GetMapping("/worker-load/{workerId}")
    public Map<String, Integer> getWorkerLoad(@PathVariable String workerId) {
        return Map.of("load", assignService.getWorkerCurrentLoad(workerId));
    }

    @GetMapping("/rules")
    public List<AssignRule> getAllRules() {
        return assignService.getAllRules();
    }

    @GetMapping("/rules/enabled")
    public AssignRule getEnabledRule() {
        return assignService.getEnabledRule().orElse(null);
    }

    @PostMapping("/rules")
    public AssignRule createRule(@RequestBody AssignRule rule) {
        return assignService.createRule(rule);
    }

    @PutMapping("/rules/{id}")
    public AssignRule updateRule(@PathVariable String id, @RequestBody AssignRule rule) {
        rule.setId(id);
        return assignService.updateRule(rule);
    }
}
