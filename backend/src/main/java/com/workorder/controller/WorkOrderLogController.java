package com.workorder.controller;

import com.workorder.model.WorkOrderLog;
import com.workorder.service.WorkOrderLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class WorkOrderLogController {

    private final WorkOrderLogService logService;

    public WorkOrderLogController(WorkOrderLogService logService) {
        this.logService = logService;
    }

    @GetMapping("/workorder/{workOrderId}")
    public List<WorkOrderLog> getByWorkOrderId(@PathVariable String workOrderId) {
        return logService.getLogsByWorkOrderId(workOrderId);
    }

    @GetMapping
    public List<WorkOrderLog> getAll() {
        return logService.getAllLogs();
    }
}
