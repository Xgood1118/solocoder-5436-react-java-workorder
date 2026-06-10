package com.workorder.model;

import lombok.Data;

import java.util.List;

@Data
public class Worker {
    private String id;
    private String name;
    private String avatar;
    private String workstation;
    private List<String> skills;
    private int currentLoad;
}
