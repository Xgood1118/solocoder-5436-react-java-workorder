package com.workorder.controller;

import com.workorder.model.WorkOrder;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping
    public List<WorkOrder> getQueue() {
        return queueService.getQueue();
    }

    @GetMapping("/status/{status}")
    public List<WorkOrder> getByStatus(@PathVariable WorkOrderStatus status) {
        return queueService.getByStatus(status);
    }

    @GetMapping("/active")
    public List<WorkOrder> getActiveWorkOrders() {
        return queueService.getActiveWorkOrders();
    }
}
