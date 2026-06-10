package com.workorder.repository;

import com.workorder.model.Worker;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WorkerRepository {

    private final Map<String, Worker> workers = new ConcurrentHashMap<>();

    public Worker save(Worker worker) {
        workers.put(worker.getId(), worker);
        return worker;
    }

    public Optional<Worker> findById(String id) {
        return Optional.ofNullable(workers.get(id));
    }

    public List<Worker> findAll() {
        return new ArrayList<>(workers.values());
    }

    public List<Worker> findByWorkstation(String workstation) {
        return workers.values().stream()
                .filter(w -> workstation.equals(w.getWorkstation()))
                .collect(Collectors.toList());
    }

    public List<Worker> findBySkill(String skill) {
        return workers.values().stream()
                .filter(w -> w.getSkills() != null && w.getSkills().contains(skill))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        workers.remove(id);
    }
}
