package com.workorder.model.enums;

public enum QualityProcessType {
    REWORK("返工"),
    SCRAP("报废");

    private final String description;

    QualityProcessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
