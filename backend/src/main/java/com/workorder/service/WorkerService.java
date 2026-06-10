package com.workorder.service;

import com.workorder.exception.BusinessException;
import com.workorder.model.Worker;
import com.workorder.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Worker create(Worker worker) {
        worker.setId(UUID.randomUUID().toString());
        worker.setCurrentLoad(0);
        return workerRepository.save(worker);
    }

    public Worker getById(String id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工人不存在: " + id));
    }

    public List<Worker> getAll() {
        return workerRepository.findAll();
    }

    public List<Worker> getByWorkstation(String workstation) {
        return workerRepository.findByWorkstation(workstation);
    }

    public Worker update(Worker worker) {
        Worker existing = getById(worker.getId());
        worker.setCurrentLoad(existing.getCurrentLoad());
        return workerRepository.save(worker);
    }

    public void delete(String id) {
        workerRepository.deleteById(id);
    }
}
