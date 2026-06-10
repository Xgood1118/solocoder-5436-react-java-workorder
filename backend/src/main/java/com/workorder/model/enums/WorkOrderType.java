package com.workorder.model.enums;

public enum WorkOrderType {
    PRODUCTION("生产工单"),
    QUALITY("质量工单"),
    EQUIPMENT("设备工单"),
    MATERIAL("物料工单"),
    MOLD("模具工单");

    private final String description;

    WorkOrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
