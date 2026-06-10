package com.workorder.model.enums;

public enum WorkOrderStatus {
    CREATED("已创建"),
    ASSIGNED("已分派"),
    ACCEPTED("已接单"),
    IN_PROGRESS("处理中"),
    SUSPENDED("已挂起"),
    COMPLETED("已完成"),
    RETURNED("已退回");

    private final String description;

    WorkOrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
