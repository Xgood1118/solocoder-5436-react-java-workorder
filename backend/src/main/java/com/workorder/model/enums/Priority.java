package com.workorder.model.enums;

public enum Priority {
    URGENT(4, "紧急"),
    HIGH(3, "高"),
    MEDIUM(2, "中"),
    LOW(1, "低");

    private final int level;
    private final String description;

    Priority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}
