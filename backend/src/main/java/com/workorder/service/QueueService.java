package com.workorder.service;

import com.workorder.model.WorkOrder;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {

    private final WorkOrderRepository workOrderRepository;

    public QueueService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public List<WorkOrder> getQueue() {
        return workOrderRepository.findQueue();
    }

    public List<WorkOrder> getByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    public List<WorkOrder> getActiveWorkOrders() {
        return workOrderRepository.findByStatusIn(
                List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.ACCEPTED,
                        WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.SUSPENDED)
        );
    }
}
