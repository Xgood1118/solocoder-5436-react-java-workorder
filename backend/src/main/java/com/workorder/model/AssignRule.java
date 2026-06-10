package com.workorder.model;

import lombok.Data;

@Data
public class AssignRule {
    private String id;
    private String name;
    private boolean enabled;
    private boolean considerWorkstation;
    private boolean considerSkills;
    private boolean considerLoad;
    private int maxLoadPerWorker;
}
